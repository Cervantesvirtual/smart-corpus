package com.cervantesvirtual.corpus;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.cervantesvirtual.index.SearchFiles;

@Controller
public class SearchController {

	@RequestMapping(value = "corpus", method = RequestMethod.GET, produces = "text/html")
    public String searchForm(Model model) {
        model.addAttribute("searchModel", new SearchModel());
        return "corpus";
    }
	
	@RequestMapping(value = "description", method = RequestMethod.GET, produces = "text/html")
    public String description(Model model) {
        model.addAttribute("searchModel", new SearchModel());
        return "description";
    }

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String searchSubmit(@ModelAttribute SearchModel searchModel) {
		
		SearchFiles searchFiles = new SearchFiles();
		try {
			searchFiles.search(searchModel);
		} catch (Exception e) {
			System.out.println("Error searching:" + e.getMessage());
		}
        return "result";
    }
}
