package com.lovepetproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.*;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;

@Controller
public class LovePetProjectController {
	static String domain = "http://animal.go.kr";
	static String baasiourl = "https://api.baas.io/19afa818-e241-11e2-9011-06530c0000b4/3a653992-e241-11e2-9011-06530c0000b4/pets";
	
	@RequestMapping(value="/todayPetList", method=RequestMethod.GET)
	public ModelAndView todayPetQuery() throws MalformedURLException, IOException, NoSuchAlgorithmException{
		// parameter
		String date = getCurrentDate();
		int pageCount = 1;
		boolean loading = true;
		
		// container
		List<PetVO> todayPetList = new ArrayList<PetVO>();
		
		// query
		while (loading) {
			List<PetVO> petList = getPetList(date, pageCount);
			
			if (petList == null) {
				loading = false;
			} else {
				todayPetList.addAll(petList);
				pageCount++;
			}
		}
		
		String petNameList = getPetNameListFromBaas(date);
		
		for (PetVO petVO : todayPetList) {
			String encodedID = encodeString(petVO.getBoardID());
			
			if (petNameList.contains(encodedID) != true) {
				String json = new Gson().toJson(petVO);
				putPetDataToBaas(petVO.getBoardID(), json);
			}
		}
		
		return new ModelAndView("todayPetList", "todayPetList", todayPetList);
	}
	
	private String getPetNameListFromBaas(String date) throws IOException {
		String sql = "?ql=select%20name%20where%20date%3D'" + date + "'&limit=100";
		URL baasUrl = new URL(baasiourl + sql);
		
		System.out.println(baasiourl + sql);
		
		HttpURLConnection conn = (HttpURLConnection) baasUrl.openConnection();
		conn.setRequestMethod("GET");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
        
		return response.toString();
	}

	private void putPetDataToBaas(String boardID, String json) throws MalformedURLException,
			IOException, ProtocolException, UnsupportedEncodingException, NoSuchAlgorithmException {
		String encodedID = encodeString(boardID);
        
		// baas.io write
		URL baasUrl = new URL(baasiourl + "/" + encodedID);
		HttpURLConnection conn = (HttpURLConnection) baasUrl.openConnection();
		
		//add request header
		conn.setRequestMethod("PUT");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Content-Length", ""+ json.length());
		
		// Send post request
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
		wr.write(json);
		wr.flush();
		wr.close();
		
//		int responseCode = conn.getResponseCode();
//		System.out.println(responseCode + " : " + response.toString());
 
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
	}
	
	private List<PetVO> getPetList(String date, int pageCount) throws MalformedURLException, IOException {
		// container
		List<PetVO> petList = new ArrayList<PetVO>();
		
		// query
		String url = domain + "/portal_rnl/abandonment/protection_list.jsp?"
				+ "s_date=" + date + "&e_date=" + date + "&pagecnt=" + pageCount;
		Source source = new Source(new URL(url));
		
		List<Element> thumbnailImgNodes = source.getAllElementsByClass("thumbnail_img01");
		List<Element> linkButtonNodes = source.getAllElementsByClass("thumbnail_btn01");

		for (int i = 0; i < thumbnailImgNodes.size(); i++) {
			PetVO vo = new PetVO();
			
			Element thumbnailImgNode = thumbnailImgNodes.get(i);
			Element thumbnailNode = thumbnailImgNode.getFirstElement(HTMLElementName.IMG);
			vo.setThumbnailSrc(domain + thumbnailNode.getAttributeValue("src"));
			
			Element linkButtonNode = linkButtonNodes.get(i);
			Element linkNode = linkButtonNode.getFirstElement(HTMLElementName.A);
			vo.setLinkSrc(domain + linkNode.getAttributeValue("href"));
			
			petList.add(vo);
		}
		
		if (0 == petList.size()) {
			return null;
		}
		
		for (int i = 0; i < petList.size(); i++) {
			PetVO vo = petList.get(i);
			source = new Source(new URL(vo.getLinkSrc()));
			
			// Image
			Element imageNode = source.getFirstElementByClass("photoArea");
			vo.setImageSrc(domain + imageNode.getAttributeValue("src"));
			
			// Text
			Element tableNode = source.getFirstElementByClass("viewTable");
			List<Element> trNodes = tableNode.getAllElements(HTMLElementName.TR);
			vo.setBoardID(trNodes.get(0).getFirstElement(HTMLElementName.TD).getContent().getTextExtractor().toString());
			vo.setPetType(trNodes.get(1).getFirstElement(HTMLElementName.TD).getContent().getTextExtractor().toString());
			vo.setColor(trNodes.get(2).getFirstElement(HTMLElementName.TD).getContent().getTextExtractor().toString());
			vo.setSex(trNodes.get(3).getFirstElement(HTMLElementName.TD).getContent().getTextExtractor().toString());
			String yearAndWeight = trNodes.get(4).getFirstElement(HTMLElementName.TD).getContent().getTextExtractor().toString();
			String[] values = yearAndWeight.split("/");
			vo.setYear(values[0].trim());
			vo.setWeight(values[1].trim());
			vo.setFoundLocation(trNodes.get(5).getFirstElement(HTMLElementName.TD).getContent().getTextExtractor().toString());
			vo.setDate(trNodes.get(6).getAllElements(HTMLElementName.TD).get(0).getContent().getTextExtractor().toString());
			// vo.setNeutralize(trNodes.get(6).getAllElements(HTMLElementName.TD).get(1).getContent().getTextExtractor().toString());
			vo.setDetail(trNodes.get(7).getFirstElement(HTMLElementName.TD).getContent().getTextExtractor().toString());
			vo.setDistrictOffice(trNodes.get(9).getFirstElement(HTMLElementName.TD).getContent().getTextExtractor().toString());
			vo.setState(trNodes.get(10).getFirstElement(HTMLElementName.TD).getContent().getTextExtractor().toString());
			vo.setCenterName(trNodes.get(11).getAllElements(HTMLElementName.TD).get(0).getContent().getTextExtractor().toString());
			vo.setCenterTel(trNodes.get(11).getAllElements(HTMLElementName.TD).get(1).getContent().getTextExtractor().toString());
			vo.setCenterLocation(trNodes.get(12).getFirstElement(HTMLElementName.TD).getContent().getTextExtractor().toString());
			
			petList.set(i, vo);
		}
		
		return petList;
	}
	
	/**
	  * @return YYYY-MM-DD 형식의 오늘 날짜
	  */
	private static String getCurrentDate(){
		 DecimalFormat df = new DecimalFormat("00");
		 Calendar currentCal = Calendar.getInstance();
	  
		 currentCal.add(currentCal.DATE, 0);
	  
		 String year = Integer.toString(currentCal.get(Calendar.YEAR));
		 String month = df.format(currentCal.get(Calendar.MONTH)+1);
		 String day = df.format(currentCal.get(Calendar.DAY_OF_MONTH));
	  
		 return year+"-"+month+"-"+day;
	}
	
	
	private String encodeString(String string) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		// encode
		StringBuffer hexString = new StringBuffer();
		byte[] bytes = string.getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] hash = md.digest(bytes);
		
		for (int i = 0; i < hash.length; i++) {
            if ((0xff & hash[i]) < 0x10) {
                hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
            } else {
                hexString.append(Integer.toHexString(0xFF & hash[i]));
            }
        }

		return hexString.toString();
	}
}
