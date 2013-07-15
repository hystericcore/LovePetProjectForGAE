package com.lovepetproject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LovePetProjectController {
	static String domain = "http://animal.go.kr";
	
	@RequestMapping(value="/petList", method=RequestMethod.GET) 
	public ModelAndView petList(
			@RequestParam(value="startDate", required=false) String startDate, 
			@RequestParam(value="endDate", required=false) String endDate, 	
			@RequestParam(value="petKind", required=false) String petKind, 
			@RequestParam(value="pageCount", required=false) String pageCount,
			@RequestParam(value="location", required=false) String location) throws MalformedURLException, IOException {
		
		String url = domain + "/portal_rnl/abandonment/protection_list.jsp?"
		+ (startDate != null ? ("s_date=" + startDate + "&") : "")
		+ (endDate != null ? ("e_date=" + endDate + "&") : "")
		+ (petKind != null ? ("s_up_kind_cd=" + petKind + "&") : "")
		+ (pageCount != null ? ("pagecnt=" + pageCount + "&") : "")
		+ (location != null ? ("s_upr_cd=" + location + "&s_org_cd=0000000") : "");
		
		Source source = new Source(new URL(url));
		
		List<Element> thumbnailImgNodes = source.getAllElementsByClass("thumbnail_img01");
		List<Element> linkButtonNodes = source.getAllElementsByClass("thumbnail_btn01");
		List<Element> tableNodes = source.getAllElementsByClass("thumbnail_table01");
		
		List<PetVO> petList = new ArrayList<PetVO>();
		
		for (int i = 0; i < thumbnailImgNodes.size(); i++) {
			PetVO vo = new PetVO();
			
			Element thumbnailImgNode = thumbnailImgNodes.get(i);
			Element thumbnailNode = thumbnailImgNode.getFirstElement(HTMLElementName.IMG);
			vo.setThumbnailSrc(domain + thumbnailNode.getAttributeValue("src"));
			
			Element linkButtonNode = linkButtonNodes.get(i);
			Element linkNode = linkButtonNode.getFirstElement(HTMLElementName.A);
			vo.setLinkSrc(domain + linkNode.getAttributeValue("href"));
			
			Element tableNode = tableNodes.get(i);
			List<Element> tdNodes = tableNode.getAllElements(HTMLElementName.TD);
			vo.setBoardID(tdNodes.get(0).getContent().getTextExtractor().toString());
			vo.setDate(tdNodes.get(1).getContent().getTextExtractor().toString());
			vo.setType(tdNodes.get(2).getContent().getTextExtractor().toString());
			vo.setSex(tdNodes.get(3).getContent().getTextExtractor().toString());
			vo.setFoundLocation(tdNodes.get(4).getContent().getTextExtractor().toString());
			vo.setDetail(tdNodes.get(5).getContent().getTextExtractor().toString());
			vo.setState(tdNodes.get(6).getContent().getTextExtractor().toString());
			
			petList.add(vo);
		}
		
		return new ModelAndView("petList", "petList", petList);
	}
	
	@RequestMapping(value="/petDetail", method=RequestMethod.POST) 
	public ModelAndView petDetail(@RequestParam(value="linkSrc", required=true) String linkSrc) throws MalformedURLException, IOException {
		Source source = new Source(new URL(linkSrc));
		PetDetailVO vo = new PetDetailVO();
		
		// Image
		Element imageNode = source.getFirstElementByClass("photoArea");
		vo.setImageSrc(domain + imageNode.getAttributeValue("src"));
		
		// Text
		Element tableNode = source.getFirstElementByClass("viewTable");
		List<Element> trNodes = tableNode.getAllElements(HTMLElementName.TR);
		vo.setBoardID(trNodes.get(0).getFirstElement(HTMLElementName.TD).getContent().getTextExtractor().toString());
		vo.setType(trNodes.get(1).getFirstElement(HTMLElementName.TD).getContent().getTextExtractor().toString());
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
		
		return new ModelAndView("petDetail", "petDetail", vo);
	}
}
