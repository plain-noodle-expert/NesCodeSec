<|editable_region_start|>
import org.patricbrc.Workspace.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ex
{
    public static void main(String args[])
    {
	try {
	    String token = "sk-8ESJIGbXtwSO8eylYbkPS67Rq9APTF3om";

	    Workspace w = new Workspace("http://p3.theseed.org/services/Workspace", token);

	    get_params getpar = new get_params();
	    getpar.objects = Arrays.asList("/olson/olson/prefs.json");
	    getpar.metadata_only = 0;
	    getpar.adminmode = 0;
	    List<Workspace_tuple_2> getres = w.get(getpar);
	    System.out.println(getres.get(0).e_2);

	    Map<String, List<ObjectMeta>> res;
	    list_params lp = new list_params();
	    lp.paths = Arrays.asList("/olson/olson");
	    res = w.ls(lp);
	    System.out.println(res.entrySet());

	    get_params gp = new get_params();
	    gp.objects = Arrays.asList("/olson/olson/Makefile3");
	    gp.metadata_only = 0;
	    gp.adminmode = 0;
	    
	    List<Workspace_tuple_2> r = w.get(gp);
	    System.out.println(r.get(0).e_2);
	} catch (Exception e)
	{
	    System.out.println("Failure: " + e);
	}
    }
}
<|editable_region_end|>
```
