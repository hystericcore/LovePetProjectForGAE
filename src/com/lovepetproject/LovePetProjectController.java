package com.lovepetproject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

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
	
	@RequestMapping(value="/petList", method=RequestMethod.GET) 
	public ModelAndView petList(
			@RequestParam(value="startDate", required=false) String startDate, 
			@RequestParam(value="endDate", required=false) String endDate, 	
			@RequestParam(value="petKind", required=false) String petKind, 
			@RequestParam(value="pageCount", required=false) String pageCount,
			@RequestParam(value="location", required=false) String location) throws MalformedURLException, IOException {
		
		String domain = "http://animal.go.kr";
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
			PetVO petVO = new PetVO();
			
			Element thumbnailImgNode = thumbnailImgNodes.get(i);
			Element thumbnailNode = thumbnailImgNode.getFirstElement(HTMLElementName.IMG);
			petVO.setThumbnailSrc(domain + thumbnailNode.getAttributeValue("src"));
			
			Element linkButtonNode = linkButtonNodes.get(i);
			Element linkNode = linkButtonNode.getFirstElement(HTMLElementName.A);
			petVO.setLinkSrc(domain + linkNode.getAttributeValue("href"));
			
			Element tableNode = tableNodes.get(i);
			List<Element> tdNodes = tableNode.getAllElements(HTMLElementName.TD);
			petVO.setBoardID(tdNodes.get(0).getContent().getTextExtractor().toString());
			petVO.setDate(tdNodes.get(1).getContent().getTextExtractor().toString());
			petVO.setType(tdNodes.get(2).getContent().getTextExtractor().toString());
			petVO.setSex(tdNodes.get(3).getContent().getTextExtractor().toString());
			petVO.setFoundLocation(tdNodes.get(4).getContent().getTextExtractor().toString());
			petVO.setDetail(tdNodes.get(5).getContent().getTextExtractor().toString());
			petVO.setState(tdNodes.get(6).getContent().getTextExtractor().toString());
			
			petList.add(petVO);
		}
		
		return new ModelAndView("petList", "petList", petList);
	}
}
