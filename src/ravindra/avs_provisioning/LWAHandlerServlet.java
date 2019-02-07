package ravindra.avs_provisioning;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Created by Ravindra on 24/9/2017.
 */
public class LWAHandlerServlet extends HttpServlet{

    String client_id = "";
    String client_secret = "";
    String product_id = "";
    String dsno = "";
    String redirect_uri="";
    String r_uri="";



    public void doGet(HttpServletRequest req,
                      HttpServletResponse res) throws IOException
    {
        Properties prop = new Properties();
        InputStream in = null;
        ResourceBundle resource = ResourceBundle.getBundle("avs-config");

        String avsproductFile= resource.getString("avsproduct_file");

        String root_path= String.valueOf(req.getRequestURL());


        try{

            //ClassLoader classLoader1 = Thread.currentThread().getContextClassLoader();
            in = new FileInputStream(avsproductFile);



            prop.load(in);

            client_id = prop.getProperty("client_id");
            client_secret = prop.getProperty("client_secret");
            product_id = prop.getProperty("product_id");
            dsno = prop.getProperty("device_serial_number");
            redirect_uri=prop.getProperty("redirect_uri");
            r_uri = root_path + redirect_uri;

            HttpSession session1=req.getSession(true);

            session1.setAttribute("client_id",client_id);
            session1.setAttribute("client_secret",client_secret);
            session1.setAttribute("product_id",product_id);
            session1.setAttribute("dsno",dsno);
            session1.setAttribute("redirect_uri",r_uri);


        }
        catch (Exception ex)
        {
            ///no product info

            res.sendRedirect(root_path +"static/ErrorProductInfo.html");
        }
        finally{
            if (in != null)
            {
                try {
                    in.close();

                }catch (IOException ex1)
                {
                    ex1.printStackTrace();
                }
            }
        }
        //ResourceBundle resource = ResourceBundle.getBundle("avsproduct");

        String scope_lwa = "alexa:all";
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter pw=res.getWriter();

        String scriptBlock="function sendFn() {\n" +
                "  //alert(\"hello\");\n" +
                "    var scope_data_str='{\"alexa:all\": { \"productID\":\"'+document.getElementById(\"product_id\").value+'\",\"productInstanceAttributes\": {\"deviceSerialNumber\": \"'+document.getElementById(\"dsno\").value+'\"}}}';\n" +
                "    var urlStr=\"https://www.amazon.com/ap/oa?\";\n" +
                "    urlStr +=\"client_id=\"+document.getElementById(\"client_id\").value;\n" +
                "    urlStr +=\"&scope=\"+document.getElementById(\"scope\").value;\n" +
                "    urlStr +=\"&scope_data=\"+scope_data_str;\n" +
                "    urlStr +=\"&response_type=code&state=\"+document.getElementById(\"redirect_uri\").value;\n" +
                "    urlStr +=\"&redirect_uri=\"+document.getElementById(\"redirect_uri\").value;\n" +
                "\n" +
                "    urlStr2=encodeURI(urlStr);\n" +
                "\n" +
                "    window.location.href = urlStr2;\n" +
                "\n" +
                "}";
   String headerBlock="<html><head><title>Amazon AVS Authorization</title><script type=\"text/javascript\" >"+scriptBlock+"</script></head>";
   headerBlock +="<body style=\"text-align:center; vertical-align:middle;\">";
    pw.write(headerBlock);

  String inputBlock="<input id=\"client_id\" type=\"text\" hidden=\"true\" name=\"client_id\" value=\""+client_id +"\" /><br/>";
  inputBlock +="<input id=\"scope\" type=\"text\"  name=\"scope\" hidden=\"true\" value=\""+scope_lwa+"\" /><br/>";
  inputBlock +="<input id=\"product_id\" type=\"text\" hidden=\"true\"  name=\"scope\" value=\""+product_id +"\" /><br/>";
  inputBlock +="<input id=\"dsno\" type=\"text\" hidden=\"true\" name=\"scope\" value=\""+dsno +"\" /><br/>";

  inputBlock +="<input id=\"redirect_uri\" type=\"text\" hidden=\"true\"  name=\"redirect_uri\" value=\""+r_uri+"\" /><br/>";
  inputBlock +="<input type=\"image\" value=\"button\" src=\"" + getServletContext().getContextPath() + "/images/LWA_btn.png\" onclick=\"sendFn();\" />";

  pw.write(inputBlock);

  pw.write ("</body></html>");
 pw.close();
    }


}
