import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;

import Parsing.Concept;
import Parsing.MemorySearcher;
import Parsing.NographSearcher;
import Parsing.Parser;
import Parsing.Searcher;
import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.ContextHandler;
import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;

//interactive link, anchor tag with href
public class RESTHandler implements ContextHandler
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
	private HTTPServer serverInstance;
	
	public void setInstance(HTTPServer server)
	{
		serverInstance = server;
	}
	
	boolean firstTime = true;
	@Override
	public int serve(Request request, Response response) throws IOException 
	{
		System.out.println(request.getPath());
		String commSel = request.getParams().get("CommSel");
		String commInp = request.getParams().get("CommInp");
		
		if(request.getPath().contains("shutDown"))
		{
			serverInstance.stop();
			System.exit(0);
		}
		
		if (firstTime)
		{
			searcher = new NographSearcher();
			firstTime = false;
		}
        //System.out.println(request.getParams().get("Search"));
		
        StringBuilder sb = new StringBuilder();
        
        if(commSel.equals("search_concepts"))
        {
        	String output = pSearchConcepts(commInp, searcher);
            sb.append(output);
        }
        else if(commSel.equals("search_desc"))
        {
        	String output = pSearchDesc(commInp, searcher);
            sb.append(output);
        }
        else if(commSel.equals("print_concepts_on_level"))
        {
        	int level = Integer.parseInt(commInp);
        	String output = pPrintConceptsOnLevel(level, searcher);
            sb.append(output);
        }
        else if(commSel.equals("print_all_concept_names"))
        {
        	String output = pPrintConceptNames(searcher);
            sb.append(output);
        }
        else if(commSel.equals("print_num_level_concept")) //depreciated
        {
        	int level = Integer.parseInt(commInp);
        	String output = pPrintNumLvlConcepts(level, searcher);
            sb.append(output);
        }
        else if(commSel.equals("print_total_concepts")) //depreciated
        {
        	String output = pPrintTotalConcepts(searcher);
            sb.append(output);
        }
        else if(commSel.equals("print_translations"))
        {
        	if (commInp != null)
        	{
            	String output = pPrintTranslations(commInp, searcher);
                sb.append(output);
        	}
        	else
        	{
            	String output = pPrintTranslations(searcher); //depreciated, I hope
                sb.append(output);
        	}
        }
        else if(commSel.equals("print_ancestors_list"))
        {
        	String output = pPrintAncestorsList(commInp, searcher);
            sb.append(output);
        }
        else if(commSel.equals("print_ancestors_names"))
        {
        	String output = pPrintAncestorsNames(commInp, searcher);
            sb.append(output);
        }
        else if(commSel.equals("print_children"))
        {
        	String output = pGetChildren(commInp, searcher);
            sb.append(output);
        }
        else if(commSel.equals("give_concept"))
        {
        	String output = pGiveConcept(commInp, searcher);
            sb.append(output);
        }
        else
        {
            sb.append("<p>");
            sb.append("Not A Valid Command Selection or Command Input");
            sb.append("</p>");
        }
        
		response.getHeaders().add("Content-Type", "application/json");
		//System.out.println(sb.toString());
		response.send(200, sb.toString());
		return 0;
	}
	
	public static String pGiveConcept(String inp, Searcher searcher)
	{
		StringBuilder sb = new StringBuilder();
		ArrayList<Concept> arr = searcher.searchConcepts(inp);
		System.out.println(arr.size());
		if(arr == null || arr.size() == 0)
		{
			sb.append("[]");
		}
		for(int i = 0; i < arr.size(); i++)
		{
			String curr = arr.get(i).getName().toLowerCase();
			if (curr.equals(inp.toLowerCase()))
			{
				System.out.println(arr.get(i));
				ArrayList<Concept> tempArr = new ArrayList<Concept>();
				tempArr.add(arr.get(i));
				JSONArray jArr = makeJArr(tempArr);
				sb.append(jArr.toString());
			}
		}	
		//sb.append("<br><br>" + pGetChildren(inp, searcher));
		//sb.append("<br><br>" + pPrintAncestorsNames(inp, searcher));
		//sb.append("<br><br>" + pPrintTranslations(inp, searcher));
		return sb.toString();
	}
	
	public static String pSearchConcepts(String inp, Searcher searcher)
	{
		StringBuilder sb = new StringBuilder();
		ArrayList<Concept> arr = searcher.searchConcepts(inp);
		if(arr == null || arr.size() == 0)
		{
			sb.append("[]");
		}
		else
		{
			JSONArray jArr = makeJArr(arr);
			sb.append(jArr.toString());
		}
		return sb.toString();
	}
	
	public static String pSearchDesc(String inp, Searcher searcher)
	{
		
		StringBuilder sb = new StringBuilder();
		ArrayList<Concept> arr = searcher.searchDesc(inp);
		if(arr == null || arr.size() == 0)
		{
			sb.append("[]");
		}
		else
		{
			JSONArray jArr = makeJArr(arr);
			sb.append(jArr.toString());
		}
		
		return pSearchConcepts(inp, searcher);
	}
	
	public static String pGetChildren(String name, Searcher searcher)
	{
		ArrayList<Concept> arr = searcher.getChildren(name, 70000);
		StringBuilder sb = new StringBuilder();
		if(arr == null || arr.size() == 0)
		{
			sb.append("[]");
		}
		else
		{
			JSONArray jArr = makeJArr(arr);
			sb.append(jArr.toString());
		}
		return sb.toString();
	}
	
	public static String pPrintConceptsOnLevel(int level, Searcher searcher)
	{
		StringBuilder sb = new StringBuilder();
		ArrayList<Concept> arr = searcher.printConcepts(level);
		System.out.println(arr.size());
		if(arr == null || arr.size() == 0)
		{
			sb.append("[]");
		}
		else
		{
			JSONArray jArr = makeJArr(arr);
			sb.append(jArr.toString());
		}
		System.out.println(arr.size());
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
		if(arr == null || arr.size() == 0)
		{
			sb.append("[]");
		}
		else
		{
			JSONArray jArr = makeJArr(arr);
			sb.append(jArr.toString());
		}
		return sb.toString();
	}
	
	public static String pPrintTotalConcepts(Searcher searcher)
	{
		int count = searcher.printTotalConcepts();
		return "Number of Total Concepts: " + count;
	}
	
	public static String pPrintTranslations(Searcher searcher) //depreciated
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
		ArrayList<String> lang = found.getLanguages();
		ArrayList<String> trans = found.getTranslations();
		ArrayList<String> combArr = new ArrayList<String>();
		
		for (int i = 0; i < lang.size(); i++)
		{
			combArr.add("language: " + lang.get(i) + ", translation: " + trans.get(i));
		}
		
		if(combArr == null || combArr.size() == 0)
		{
			sb.append("[]");
		}
		else
		{
			JSONArray jArr = new JSONArray(combArr);
			sb.append(jArr.toString());
		}
		System.out.println(combArr.toString());
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
		ArrayList<Concept> ancestors = searcher.printAncestorsList(name);
		StringBuilder sb = new StringBuilder();
		if(ancestors == null || ancestors.size() == 0)
		{
			sb.append("[]");
		}
		else
		{
			JSONArray jArr = makeJArr(ancestors);
			sb.append(jArr.toString());
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
	
	public static JSONArray makeJArr(ArrayList<Concept> arr)
	{
		List<Concept> tempArr = arr;
		if (arr.size() > 30000)
		{
			tempArr = arr.subList(0, 30000); //hard coded 30000 concept limit
		}
		JSONArray jArr = new JSONArray(tempArr);
		return jArr;
	}
}
