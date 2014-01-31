package test.widgetdelclima;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/******************************************************************
 * 
 * 	   WIDGET DEL CLIMA
 * 	
 * 	   AUTOR: SEBASTIAN CIPOLAT 
 *     BUENOS AIRES ARGENTINA 2012
 *
 *		VERSION PARA ANDROIDEITY
 *
 *******************************************************************/


public class Miwidget extends AppWidgetProvider{
					
		Document weatherDoc ;
	    MyWeather weatherResult;
	    String weatherString ;	     
	    private String ciudad,temp,unit_temp;
		private static String maxT;
	    private static String minT=null;
		public static int yahoo_code;
	   
        private static final String ACTION_update = "force_update";  

	
	 @Override
	    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
	    int[] appWidgetIds) {

		 /*Cada  vez que se ejecute onUpdate ejecuta get_data qeu se encargara de tomar los datos de internet
		  * y devolverlos como un objeto Document. 
		  */
		 
		 get_data(context  ,  appWidgetManager);
		 
		 }
	 
	 
	 
	 public  int get_data(Context ctx  ,   AppWidgetManager appwidg){
		 
	 weatherString= QueryYahooWeather("468739"); //WOEID de Buenos Aires 468739
					 
	/*Si weatherString retorna un String diferente a null 
	  significa que QueryYahooWeather devolvio algun dato por lo que podemos continuar en caso de que fuese null
	  mostrara error y pasara a actualizarWidget un parametro 0 para que muestre error 
	  */  
					 
		if (weatherString!=null){		 
		 //convertimos weatherString a Document
		weatherDoc = convertStringToDocument(weatherString);
						 
		if (weatherDoc!=null){//si todo estuvo bien parseamos el documento
					 
		weatherResult= parseWeather(weatherDoc);
					 			
					 			
			if (weatherResult!=null){ 
			/*Si llegamos hasta aca significa que todo funciono bien y
			* pasamos a actualizarWidget las variables a mostrar. 
			*/
			actualizarWidget(ctx, appwidg, weatherResult.city,weatherResult.conditiontemp,weatherResult.unit_temp
			,weatherResult.MaxT,weatherResult.MinT,weatherResult.YahooWcode,1);
			//actualizarWidget(ctx, appwidg, ciudad,temp,unit_temp,1);
						 			
			                       }
					 		}
					 		
					 	}else{
					 	Log.e("Error obteniendo informacion", ":X");
					 		
					 		//si no muestro error 
					 		//actualizarWidget(ctx, appwidg, "Error","E","r",0);
					 		//como hubo algun error no pasamos ningun parametro excepto el erro_flag
		                 	actualizarWidget(ctx, appwidg,null,null,null,null,null,null,0);
					 	} 
					 
		 return 0;
	 }
	 
	 
	 /* Realiza una peticion mediante GET al servicio de Yahoo pasando los parametros de la ciudad woid
	  * y unidad de temperatura.
	  * Retorna un String con todo el XML que luego se utilizara para extraer los datos.
	  */
	 private String QueryYahooWeather(String woid){
	    	
	    	String qResult = "";
	    	String queryString = "http://weather.yahooapis.com/forecastrss?w="+woid+"&u=c";
	    	
	    	HttpClient httpClient = new DefaultHttpClient();
	        HttpGet httpGet = new HttpGet(queryString);
	        
	        try {
	        	HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();
	        	
	        	if (httpEntity != null){
	        		InputStream inputStream = httpEntity.getContent();
	        		Reader in = new InputStreamReader(inputStream);
	        		BufferedReader bufferedreader = new BufferedReader(in);
	        		StringBuilder stringBuilder = new StringBuilder();
	        		
	        		String stringReadLine = null;
	                //imprime todo el xml en qResult
	        		while ((stringReadLine = bufferedreader.readLine()) != null) {
	        			stringBuilder.append(stringReadLine + "\n");	
	        		}
	        		
	        		qResult = stringBuilder.toString();	
	        	}

			} catch (ClientProtocolException e) {
				e.printStackTrace();
				Log.e("error! QueryYahooWeather", " :/");
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				Log.e("error! QueryYahooWeather", " :/");
				return null;
			}
	    	
	        return qResult;
	    } 
	 
	 
	 /* En base al parametro  src el cual es todo el xml devuelto por yahoo
	  * se lo pasa a un objeto del tipo Document que luego se utilizara para poder 
	  * parsear del xml lo que nos interesa
	  */
	  private Document convertStringToDocument(String src){
	    	Document dest = null;
	    	
	    	DocumentBuilderFactory dbFactory =	DocumentBuilderFactory.newInstance();
	    	DocumentBuilder parser;

	    	try {
	    		parser = dbFactory.newDocumentBuilder();
				dest = parser.parse(new ByteArrayInputStream(src.getBytes()));
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
				 
	    				
			} catch (SAXException e) {
				e.printStackTrace();
			
			} catch (IOException e) {
				e.printStackTrace();
				
			}
	    	
	    	return dest;
	    }
	    
	 
/* Este metodo es el mas importante se encargara de 'parsear' el objeto Document y extrae lo que nos interesa 
 * y asignarlo a las variables correspondientes.
 * retorna 	un objeto weatherResult del cual extraeremos los datos necesarios.
 */
		 
		 private MyWeather parseWeather(Document srcDoc){
			//creamos un objeto myWeather	
		    MyWeather myWeather = new MyWeather();
		    		   
		    /* XML  <yweather:location city="Buenos Aires" region="" country="Argentina"/>
		     * Del nodo yweather:location extrae el valor del atributo  city
		     */		    
		    Node locationNode = srcDoc.getElementsByTagName("yweather:location").item(0);
		    myWeather.city = locationNode.getAttributes().getNamedItem("city").getNodeValue().toString();
		    //ciudad=myWeather.city;
		    	
		   /* XML <yweather:units temperature="C" distance="km" pressure="mb" speed="km/h"/>
			* Del nodo yweather:units extrae el valor atributo temperature 
		    */				
			Node unittempNode = srcDoc.getElementsByTagName("yweather:units").item(0);
			myWeather.unit_temp= unittempNode.getAttributes().getNamedItem("temperature").getNodeValue().toString();

			//unit_temp= unittempNode.getAttributes()	.getNamedItem("temperature").getNodeValue().toString();
				
			/*XML <yweather:condition text="Cloudy" code="26" temp="16" date="Sat, 19 May 2012 7:00 pm ART"/>
			* Obtiene el valor de atributo temp el cual es la temperatura actual y el codigo code que utilizaremos luego
			* Tambien se observa que en text se encuetra la descripcion del clima en este caso nublado */
			
			Node conditionNode = srcDoc.getElementsByTagName("yweather:condition").item(0);
			myWeather.conditiontemp = conditionNode.getAttributes().getNamedItem("temp").getNodeValue().toString();
//			temp=myWeather.conditiontemp;
            myWeather.YahooWcode= conditionNode.getAttributes().getNamedItem("code").getNodeValue().toString();
   		    //yahoo_code=Integer.parseInt(myWeather.YahooWcode);
						
   		    /*XML <yweather:forecast day="Sat" date="19 May 2012" low="14" high="17" text="Showers Late" code="45"/>
   		     * 
   		     * Toma los valores low y high el cual son las temperaturas maxima y minima del dia.
   		     *
   		     * */
   
			Node forecastNode = srcDoc.getElementsByTagName("yweather:forecast").item(0);
			myWeather.MinT = forecastNode.getAttributes().getNamedItem("low").getNodeValue().toString();			
			minT=myWeather.MinT;
						
		    myWeather.MaxT = forecastNode.getAttributes().getNamedItem("high").getNodeValue().toString();
			maxT=myWeather.MaxT;

			/* A modo de control imprimo en el Logcat  	el contenido del objeto myWeather.toString*/			
				
				Log.e("-------", "---------------");
				Log.e("XML yw", myWeather.toString());
            
				//Devolvemos myWeather el cual se utilizara luego.
				return myWeather;
		    }
		    
		 
/* Este es el metodo que se encargara de actualizar el widget de la misma forma que vimos en el segunda parte del tutorial
 * se les pasan por parametros los valores que mostrara el temperatura , ciudad, temperatura maxy , min y un ultimo parametro  
 * error_flag el cual se utiliza para mostrar error.
 * Tambien se define el PendingIntent que utilizaremos para actualizar el contenido el widget. 
 **/
		 
	 public static void actualizarWidget(Context context,AppWidgetManager appWidgetManager, String city,String temp,
	 String unit_tmp,String tmp_max,String tmp_min,String yahoowcode,int error_flag)
		{				 
		 int img_var;
		  /****************************
		   * int error_flag
		   * 1 OK
		   * 0 Error
		   *****************************/
         RemoteViews remoteViews; 

         ComponentName thisWidget;
                
         remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
         
		  thisWidget = new ComponentName(context, Miwidget.class);
		 
		  if(error_flag==1){// Todo estuvo bien 
		/*En base al yahoo code determineimg2use nos retorna el id de la imagen a mostrar*/
		 img_var=determineimg2use(Integer.valueOf(yahoowcode)); 
			  	
		   
				   if(img_var!=-1)
					   remoteViews.setImageViewResource(R.id.clima_img,img_var);
				   else//img var=-1 hubo algun error mostramos la imagen unknown
					   remoteViews.setImageViewResource(R.id.clima_img,R.drawable.unknown);
					   
		   //Asignamos a cada elemento del lyput el texto correspondiente 
           remoteViews.setTextViewText(R.id.temp,temp+"°"+unit_tmp);
           remoteViews.setTextViewText(R.id.max_min,"Máx: " + maxT+"°"+unit_tmp+"      "+"Mín: "+minT+"°"+unit_tmp);
           remoteViews.setTextViewText(R.id.city,city);
		  
		  }else{//error_flag=0 mostramos mensaje de error 
			  
			   Toast.makeText(context,"Error: \n"+"Verifique su conexion a internet \n"+"Presione actualizar para intentarlo de nuevo.", Toast.LENGTH_LONG).show();
			   
			   remoteViews.setImageViewResource(R.id.clima_img,R.drawable.unknown);
	           remoteViews.setTextViewText(R.id.temp,"- - -");
	           remoteViews.setTextViewText(R.id.city,"ERROR");
	 		   remoteViews.setTextViewText(R.id.max_min,"No se obtuvieron datos");
	           
		  }
           
	  //Creamos un intent a nuestra propia clase como vimos en la entrega anterior
		   Intent intent = new Intent(context, Miwidget.class);
		  
		   //seleccionamos la accion ACTION_cambiarlayout
           intent.setAction(ACTION_update);
           
           PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
           remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);                        
          
           appWidgetManager.updateAppWidget(thisWidget, remoteViews);

		}
	 
   
     @Override
	 public void onReceive(Context context, Intent intent) {
		 //Controlamos que la accion recibida sea la nuestra    	     	 
        if (intent.getAction().equals(ACTION_update)) {
       	 Log.e("MANUAL UPDATE"," ACTIVATED");
       	 //actualizamos el widget.
         AppWidgetManager widgetManager =AppWidgetManager.getInstance(context);         
		 
         get_data(context  ,  widgetManager);       	 

        }
        
  super.onReceive(context, intent);
	}
	 
	 
	   		   
//Este metodo lo que hace es en base al yahoowcode retorna el id de la imagen a mostrar   
private static int determineimg2use(int yahoowcode){
	    		int finalcode;
	    	switch(yahoowcode){
	    		
	    		case 0: finalcode= -1;
	    		break;
	    		case 1: finalcode= -1;
	    		break;
	    		case 2: finalcode= -1;
	    		break;
	    		case 3: finalcode= R.drawable.img03;
	    		break;
	    		case 4: finalcode= R.drawable.img03;
	    		break;
	    		case 5: finalcode= R.drawable.img07;
	    		break;
	    		case 6: finalcode= R.drawable.img06;
	    		break;
	    		case 7: finalcode= R.drawable.img06;;
	    		break;
	    		case 8: finalcode= R.drawable.img10;
	    		break;
	    		case 9: finalcode= R.drawable.img12;
	    		break;
	    		case 10: finalcode= R.drawable.img10;
	    		break;
	    		case 11: finalcode= R.drawable.img12;
	    		break;
	    		case 12: finalcode= R.drawable.img12;
	    		break;
	    		case 13: finalcode= R.drawable.img12;
	    		break;
	    		case 14: finalcode= R.drawable.img15;
	    		break;
	    		case 15: finalcode= R.drawable.img15;
	    		break;
	    		case 16: finalcode= R.drawable.img15;
	    		break;
	    		case 17: finalcode= R.drawable.img18;
	    		break;
	    		case 18: finalcode= R.drawable.img10;
	    		break;
	    		case 19: finalcode= R.drawable.img21;
	    		break;
	    		case 20: finalcode= R.drawable.img22;
	    		break;
	    		case 21: finalcode= R.drawable.img22;
	    		break;
	    		case 22: finalcode= R.drawable.img21;
	    		break;
	    		case 23: finalcode= R.drawable.img26;
	    		break;
	    		case 24: finalcode= R.drawable.img24;
	    		break;
	    		case 25: finalcode=  R.drawable.img25;
	    		break;
	    		case 26: finalcode=  R.drawable.img26;
	    		break;
	    		case 27: finalcode=  R.drawable.img27;
	    		break;
	    		case 28: finalcode=  R.drawable.img28;
	    		break;
	    		case 29: finalcode=  R.drawable.img29;
	    		break;
	    		case 30: finalcode=  R.drawable.img30;
	    		break;
	    		case 31: finalcode=  R.drawable.img31;
	    		break;
	    		case 32: finalcode=  R.drawable.img32;
	    		break;
	    		case 33: finalcode=  R.drawable.img33;
	    		break;
	    		case 34: finalcode=  R.drawable.img34;
	    		break;
	    		case 35: finalcode=  R.drawable.img18;
	    		break;
	    		case 36: finalcode=  R.drawable.img36;
	    		break;
	    		case 37: finalcode=  R.drawable.img03;
	    		break;
	    		case 38: finalcode=  R.drawable.img03;
	    		break;
	    		case 39: finalcode=  R.drawable.img03;
	    		break;
	    		case 40: finalcode=  R.drawable.img40;
	    		break;
	    		case 41: finalcode=  R.drawable.img15;
	    		break;
	    		case 42: finalcode=  R.drawable.img13;
	    		break;
	    		case 43: finalcode=  R.drawable.img15;
	    		break;
	    		case 44: finalcode=  R.drawable.img26;
	    		break;
	    		case 45: finalcode=  R.drawable.img40;
	    		break;
	    		case 46: finalcode=  R.drawable.img07;
	    		break;
	    		case 47: finalcode=  R.drawable.img39;
	    		break;
	    		case 3200: finalcode= -1;
	    		break;
	    			    		
	    		default:
	    		 finalcode= -2;
	    		 break;
		    			    		
	    	}
	    		    
	    		return finalcode;
	    }


}
