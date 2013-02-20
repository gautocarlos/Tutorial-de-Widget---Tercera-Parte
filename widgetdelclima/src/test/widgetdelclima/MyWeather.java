package test.widgetdelclima;


public class MyWeather {
	
		String city; 			//ciudad
		String unit_temp;		//unidad de temperatura ºF / ºC
		String conditiontemp;	//Temperatura
		String YahooWcode;		//Codigo de Clima
		String MaxT;     		//Temperatura Maxima
		String MinT;			//Temperatura Minima
		

		public String toString(){ //retorna un string compuesto por todos los datos anteriores.

			return "\n- " 
					+ "city: " + city + "\n"					
					+ "unit_temp: "+unit_temp+"\n\n"				
					+"YahooWcode"+YahooWcode+ "\n"
					+ "Condition: " + conditiontemp ;
		}
	}

