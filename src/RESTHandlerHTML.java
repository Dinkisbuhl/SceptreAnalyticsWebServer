import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import Parsing.Concept;
import Parsing.MemorySearcher;
import Parsing.NographSearcher;
import Parsing.Parser;
import Parsing.Searcher;
import net.freeutils.httpserver.HTTPServer.ContextHandler;
import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;

//interactive link, anchor tag with href
public class RESTHandlerHTML implements ContextHandler
{
	//make the searcher here, available all the time
	private static ArrayList<Concept> concepts = new ArrayList<Concept>();
	private static ArrayList<Concept> sortedConcepts = new ArrayList<Concept>();
	private static ArrayList<Concept> alphaSortedConcepts = new ArrayList<Concept>();
	private static int level1S = 0;
	private static int level2S = 0;
	private static int level3S = 0;
	private static int level4S = 0;
	private static int level5S = 0;
	private static int dupNum = 0;
	
	private static Searcher searcher;
	
	public static boolean shutItDown = false;
	
	boolean firstTime = true;
	@Override
	public int serve(Request request, Response response) throws IOException 
	{
		String commSel = request.getParams().get("CommSel");
		String commInp = request.getParams().get("CommInp");
		String shutDownOption = request.getParams().get("ShutDown");
		
		System.out.println("Shut Down Param = " + shutDownOption);
		if (firstTime)
		{
			searcher = new NographSearcher();
			firstTime = false;
		}
        System.out.println(request.getParams().get("Search"));
		
        StringBuilder sb = new StringBuilder();
        
        sb.append("<!DOCTYPE html>\n"
        		+ "<html>\n"
        		+ "<head>\n"
        		+ "	<meta charset=\"utf-8\">\n"
        		+ "	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
        		+ "	<title> \n"
        		+ "REST Response"
        		+ "	</title>\n"
        		+ "</head>\n"
        		+ "<body>");
        sb.append("<p>");
        sb.append(commSel);
        sb.append("</p>");
        sb.append("<p>");
        sb.append(commInp);
        sb.append("</p>");
        
        if(shutDownOption.equals("yes"))
        {
        	shutItDown = true;
        	System.out.println("Shut Down was yes");
        }
        
        if(commSel.equals("search_concepts"))
        {
        	String output = pSearchConcepts(commInp, searcher);
            sb.append("<p>");
            sb.append(output);
            sb.append("</p>");
        }
        else if(commSel.equals("search_desc"))
        {
        	String output = pSearchDesc(commInp, searcher);
            sb.append("<p>");
            sb.append(output);
            sb.append("</p>");
        }
        else if(commSel.equals("print_concepts_on_level"))
        {
        	int level = Integer.parseInt(commInp);
        	String output = pPrintConceptsOnLevel(level, searcher);
            sb.append("<p>");
            sb.append(output);
            sb.append("</p>");
        }
        else if(commSel.equals("print_all_concept_names"))
        {
        	String output = pPrintConceptNames(searcher);
            sb.append("<p>");
            sb.append(output);
            sb.append("</p>");
        }
        else if(commSel.equals("print_num_level_concept"))
        {
        	int level = Integer.parseInt(commInp);
        	String output = pPrintNumLvlConcepts(level, searcher);
            sb.append("<p>");
            sb.append(output);
            sb.append("</p>");
        }
        else if(commSel.equals("print_total_concepts"))
        {
        	String output = pPrintTotalConcepts(searcher);
            sb.append("<p>");
            sb.append(output);
            sb.append("</p>");
        }
        else if(commSel.equals("print_translations"))
        {
        	if (commInp != null)
        	{
            	String output = pPrintTranslations(commInp, searcher);
                sb.append("<p>");
                sb.append(output);
                sb.append("</p>");
        	}
        	else
        	{
            	String output = pPrintTranslations(searcher);
                sb.append("<p>");
                sb.append(output);
                sb.append("</p>");
        	}
        }
        else if(commSel.equals("print_ancestors_list"))
        {
        	String output = pPrintAncestorsList(commInp, searcher);
            sb.append("<p>");
            sb.append(output);
            sb.append("</p>");
        }
        else if(commSel.equals("print_ancestors_names"))
        {
        	String output = pPrintAncestorsNames(commInp, searcher);
            sb.append("<p>");
            sb.append(output);
            sb.append("</p>");
        }
        else if(commSel.equals("print_children"))
        {
        	String output = pGetChildren(commInp, searcher);
            sb.append("<p>");
            sb.append(output);
            sb.append("</p>");
        }
        else if(commSel.equals("give_concept"))
        {
        	String output = pGiveConcept(commInp, searcher);
            sb.append("<p>");
            sb.append(output);
            sb.append("</p>");
        }
        else
        {
            sb.append("<p>");
            sb.append("Not A Valid Command Selection or Command Input");
            sb.append("</p>");
        }
        
        sb.append("</body>");
        sb.append("</html>");
        
		response.getHeaders().add("Content-Type", "text");
		response.send(200, sb.toString());
		return 0;
	}
	
	public static String pGiveConcept(String inp, Searcher searcher)
	{
		StringBuilder sb = new StringBuilder();
		ArrayList<Concept> arr = searcher.searchConcepts(inp);
		for(int i = 0; i < arr.size(); i++)
		{
			String curr = arr.get(i).getName().toLowerCase();
			if (curr.equals(inp.toLowerCase()))
			{
				sb.append(arr.get(i));
			}
		}	
		sb.append("<br><br>" + pGetChildren(inp, searcher));
		sb.append("<br><br>" + pPrintAncestorsNames(inp, searcher));
		sb.append("<br><br>" + pPrintTranslations(inp, searcher));
		return sb.toString();
	}
	
	public static boolean statusShutDown()
	{
		return shutItDown;
	}
	
	public static String pSearchConcepts(String inp, Searcher searcher)
	{
		StringBuilder sb = new StringBuilder();
		ArrayList<Concept> arr = searcher.searchConcepts(inp);
		if(arr.size() == 0)
		{
			sb.append("No Matches");
		}
		else
		{
			sb.append("<ul>");
			for(int i = 0; i < arr.size(); i++)
			{
				sb.append("<li><a target=\"_blank\" href=\"http://localhost:9000/rest?CommSel=give_concept&CommInp=" + arr.get(i).getName() + "\">" + arr.get(i).getName() + "</a></li>");
			}
			sb.append("</ul>");
		}
		return sb.toString();
	}
	
	public static String pSearchDesc(String inp, Searcher searcher)
	{
		StringBuilder sb = new StringBuilder();
		ArrayList<Concept> arr = searcher.searchDesc(inp);
		if(arr.size() == 0)
		{
			sb.append("No Matches");
		}
		else
		{
			for(int i = 0; i < arr.size(); i++)
			{
				sb.append("From " + arr.get(i).getName() + ":");
				sb.append("<br>" + arr.get(i).getDesc() + "<br><br>");
			}
		}
		return sb.toString();
	}
	
	public static String pGetChildren(String name, Searcher searcher)
	{
		ArrayList<Concept> arr = searcher.searchDesc(name);
		StringBuilder sb = new StringBuilder();
		if(arr.size() == 0)
		{
			sb.append("name not found");
		}
		else
		{
			sb.append("Children of " + name + ": ");
			boolean first = true;
			for(int i = 0; i < arr.size(); i++)
			{
				if (first)
				{
					sb.append(arr.get(i).getName());
					first = false;
				}
				else
				{
					sb.append(", " + arr.get(i).getName());
				}
			}
		}
		return sb.toString();
	}
	
	public static String pPrintConceptsOnLevel(int level, Searcher searcher)
	{
		StringBuilder sb = new StringBuilder();
		ArrayList<Concept> arr = searcher.printConcepts(level);
		boolean first = true;
		for(int i = 0; i < arr.size(); i++)
		{
			if (first)
			{
				sb.append(arr.get(i));
				first = false;
			}
			else
			{
				sb.append("<br><br>" + arr.get(i));
			}
		}
		return sb.toString();
	}
	
	public static String pPrintNumLvlConcepts(int level, Searcher searcher)
	{
		int count = searcher.printNumLvlConcepts(level);
		return "Number of Level " + level + " Concepts: " + count;
	}
	
	public static String pPrintConceptNames(Searcher searcher)
	{
		StringBuilder sb = new StringBuilder();
		int width = 70000;
		ArrayList<Concept> arr = searcher.printConceptNames(width);
		boolean first = true;
		for(int i = 0; i < arr.size(); i++)
		{
			if (first)
			{
				sb.append(arr.get(i).getName());
				first = false;
			}
			else
			{
				sb.append(", " + arr.get(i).getName());
			}
		}
		return sb.toString();
	}
	
	public static String pPrintTotalConcepts(Searcher searcher)
	{
		int count = searcher.printTotalConcepts();
		return "Number of Total Concepts: " + count;
	}
	
	public static String pPrintTranslations(Searcher searcher)
	{
		ArrayList<Concept> arr = searcher.printTranslations();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < arr.size(); i++)
		{
			Concept curr = arr.get(i);
			ArrayList<String> lang = curr.getLanguages();
			ArrayList<String> trans = curr.getTranslations();
			sb.append(curr.getName() + " Translations: ");
			for(int j = 0; j < lang.size(); j++)
			{
				sb.append(lang.get(j) + ": " + trans.get(j) + ", ");
			}
		}
		return sb.toString();
	}
	
	public static String pPrintTranslations(String name, Searcher searcher)
	{
		Concept found = searcher.printTranslations(name);
		StringBuilder sb = new StringBuilder();
		if (found == null)
		{
			sb.append("name not found");
		}
		else
		{
			ArrayList<String> lang = found.getLanguages();
			ArrayList<String> trans = found.getTranslations();
		 	sb.append(found.getName() + " Translations: ");
			for(int j = 0; j < lang.size(); j++)
			{
				sb.append("<br>" + lang.get(j) + ": " + trans.get(j));
			}
		}
		return sb.toString();
	}
	
	public static String pPrintAncestorsList(String name, Searcher searcher)
	{
		ArrayList<Concept> found = searcher.printAncestorsList(name);
		StringBuilder sb = new StringBuilder();
		if(found == null)
		{
			sb.append("name not found");
		}
		else
		{
			sb.append("Ancestors of " + name + ": ");
			for(int i = 0; i < found.size(); i++)
			{
				sb.append("<br><br>" + found.get(i));
			}
		}
		return sb.toString();
	}
	
	public static String pPrintAncestorsNames(String name, Searcher searcher)
	{
		//Concept found = searcher.printAncestorsNames(name);
		ArrayList<String> ancestors = searcher.printAncestorsNames(name);
		StringBuilder sb = new StringBuilder();
		if(ancestors.size() >= 0)
		{
			sb.append("Ancestors of " + name + ": ");
			/*
			System.out.println(ancestors);
			System.out.println(ancestors.getClass());
			System.out.println(ancestors.get(0).getClass());
			
			if(ancestors.get(0).getClass().equals(String.class))
			{
				System.out.println("thanks");
			*/
			boolean first = true;
			for(int j = 0; j < ancestors.size(); j++)
			{
				if (first)
				{
					sb.append(ancestors.get(j));
					first = false;
				}
				else
				{
					sb.append(", " + ancestors.get(j));
				}
			}
		}
		else
		{
			sb.append("name not found");
		}
		return sb.toString();
	}
	
	public static void calculateAncestorsAverage(Searcher searcher)
	{
		int count = searcher.printTotalConcepts();
		ArrayList<Concept> arr = searcher.printTranslations();
		int numerator = 0;
		for(int i = 0; i < count; i++)
		{
			Concept curr = arr.get(i);
			ArrayList<Concept> ancestors = curr.getAncestors();
			numerator += ancestors.size();
		}
		int avg = numerator / count;
		int mod = numerator % count;
		System.out.println("Ancestors Concept Average: ");
		System.out.println("Average: " + avg);
		System.out.println("Mod: " + mod);
		System.out.println("Numerator: " + numerator);
		System.out.println("Count: " + count);
	}
	
	public static void calculateAncestorsAverage(int level, Searcher searcher)
	{
		int count = searcher.printTotalConcepts();
		int count2 = 0;
		ArrayList<Concept> arr = searcher.printTranslations();
		int numerator = 0;
		for(int i = 0; i < count; i++)
		{
			Concept curr = arr.get(i);
			if(curr.getLevel() == level)
			{
				ArrayList<Concept> ancestors = curr.getAncestors();
				numerator += ancestors.size();
				count2++;
			}
		}
		int avg = numerator / count2;
		int mod = numerator % count2;
		System.out.println("Level " + level + ":");
		System.out.println("Average: " + avg);
		System.out.println("Mod: " + mod);
		System.out.println("Numerator: " + numerator);
		System.out.println("Count: " + count2);
	}
	
	public static void calculateChildAverage(Searcher searcher)
	{
		int count = searcher.printTotalConcepts();
		ArrayList<Concept> arr = searcher.printTranslations();
		int numerator = 0;
		for(int i = 0; i < count; i++)
		{
			Concept curr = arr.get(i);
			ArrayList<Concept> children = curr.getChildren();
			numerator += children.size();
		}
		int avg = numerator / count;
		int mod = numerator % count;
		System.out.println("Children Concepts Average: ");
		System.out.println("Average: " + avg);
		System.out.println("Mod: " + mod);
		System.out.println("Numerator: " + numerator);
		System.out.println("Count: " + count);
	}
}
