/**
 * Created by Robert Maier on 05.07.2016.
 */
package AlexThesis;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class AlexThesis {

    public static LinkedList<Integer> sizes = new LinkedList<>();
    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        Random rnd = new Random();

        String[] alex = {"du Zigeuner!", "du Beidl!", "Thesis soll scheiss'n gehen!", "Mr. President!"};
        System.out.println("Hallo Alex, "+alex[rnd.nextInt(3)]);



        System.out.println("Please enter the filepath of your data file:");
        String file = reader.nextLine();
        System.out.println("Does your file have headings (Y/N)?");
        String headings = reader.nextLine();
        headings = headings.toUpperCase();
        //System.out.println("Does this file include (1) only stock information or (2) all stock data? (Please enter number)");
        int dataType = 1;
        System.out.println("Please enter the length of the observation window in days.");
        int observationLength = reader.nextInt();
        System.out.println("Please enter the length of the event window in days.");
        int eventWindow = reader.nextInt();



        //LinkedList<StockReturn> returns = new LinkedList<StockReturn>();
        //79 Werktage vorher, 10 Tage vor Event, Event, 10 Tage nachher
        if(dataType == 2){
            DataFile data = new DataFile(file,(headings.contains("Y")));
            for(int i = 0;i<data.length()-1;i++){
                System.out.println(data.getCurrentLine().length);
                //returns.add(new StockReturn(data.getNextLine()));
                try{
                    writeToFile(new StockReturn(data.getNextLine()).toString(),"2016-07-26.txt");
                } catch(Exception e){e.printStackTrace();}
            }
        }
        else if(dataType == 1){
            DataFile data = new DataFile(file,(headings.contains("Y")));
            //System.out.println("Please enter the event window in days:");
            //int eventWindow = reader.nextInt();
            for(int i = 0; i<data.length()-1;i++){
                sizes = new LinkedList<Integer>();
                ArrayList<String> stockData = new ArrayList<>();//7 (data) + (observation window * 4 + event window * 2 * 4 + 4 (event date)
                String[] temp = data.getNextLine();
                stockData.add(temp[0]); //DealNumber
                stockData.add(temp[1]); //DealStatus
                stockData.add(temp[2]); //AcquirerName
                stockData.add(temp[3]); //Symbol
                stockData.add(temp[4]); //RumorDate
                stockData.add(temp[5]); //Withdrawal Date
                stockData.add(temp[6]); //Index
                System.out.println("Event 1 - Stock - "+stockData.get(2));
                String[] temp2 = getData(temp[4],temp[3],observationLength,eventWindow);
                for(int j = 0;j<temp2.length;j++){
                    stockData.add(temp2[j]);
                }
                System.out.println("Event 1 - Index - "+stockData.get(6));
                temp2 = getData(temp[4],temp[6],observationLength,eventWindow);
                //System.out.println(temp2[0]+temp2[temp2.length-2]);

                for(int j = 0;j<temp2.length;j++){
                    stockData.add(temp2[j]);
                }
                System.out.println("Event 2 - Stock - "+stockData.get(2));
                temp2 = getData(temp[5],temp[3],observationLength,eventWindow);

                for(int j = 0;j<temp2.length;j++){
                    stockData.add(temp2[j]);
                }
                System.out.println("Event 2 - Index - "+stockData.get(6));
                temp2 = getData(temp[5],temp[6],observationLength,eventWindow);

                for(int j = 0;j<temp2.length;j++){
                    stockData.add(temp2[j]);
                }

                String[] stockDataString = new String[stockData.size()];
                try{
                    writeToFile(stockData.toString(),"pureData.txt");
                }catch(Exception e){e.printStackTrace();}

                stockDataString = stockData.toArray(stockDataString);
                try{
                    writeToFile(new Security(stockDataString, observationLength, eventWindow,sizes).toString(),"2016-07-26.txt");

                } catch(Exception e){e.printStackTrace();}
                //System.out.println("Press Any Key To Continue...");
                //new java.util.Scanner(System.in).nextLine();
            }

        }
        else{
            System.out.println("Error: "+file+"; "+headings+"; "+dataType);
        }

    }
    private static double round(double d){
        double temp = d*100;
        temp = Math.round(temp);
        return temp/100;
    }

    static public String[]getData(String eventDate, String symbol, int observationWindow, int eventWindow){
        String fromDate = "", toDate = "";
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date d = dateFormat.parse(eventDate);
            fromDate    =   dateFormat.format(subtractWeekdays(d,(observationWindow+eventWindow+1)));
            toDate      =   dateFormat.format(addWeekdays(d,eventWindow));

        } catch(Exception ex) {
            ex.printStackTrace();
        }


        //System.out.println(dataURL);

        LinkedList<String> returnData = getTickerData(buildAPIURL(fromDate,toDate,symbol));
        //System.out.println(returnData);
        //System.out.println(returnData.size());
        int i = observationWindow+eventWindow+2;
        //System.out.println("i: "+i);
        //System.out.println("ReturnData.size() vor Schleife: "+returnData.size());
        while(returnData.size()!=((observationWindow+eventWindow*2+1)*2)){
            if(returnData.size()<((observationWindow+eventWindow*2+1)*2)){
                try {
                    //System.out.println(returnData.size());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date d = dateFormat.parse(eventDate);
                    fromDate    =   dateFormat.format(subtractWeekdays(d,i));
                    returnData = new LinkedList<>();
                    returnData = getTickerData(buildAPIURL(fromDate,toDate,symbol));
                    //System.out.println(returnData.size());

                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                i++;
            }
            else{
                try {
                    //System.out.println(returnData.size());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date d = dateFormat.parse(eventDate);
                    fromDate    =   dateFormat.format(subtractWeekdays(d,i));
                    returnData = new LinkedList<>();
                    returnData = getTickerData(buildAPIURL(fromDate,toDate,symbol));
                    //System.out.println(returnData.size());

                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                i--;
            }
        }
        //System.out.println(fromDate +" - "+toDate);
        /*while(returnData.size()<((observationWindow+eventWindow*2+1)*2))
        {

            try {
                //System.out.println(returnData.size());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date d = dateFormat.parse(eventDate);
                fromDate    =   dateFormat.format(subtractWeekdays(d,i));
                returnData = new LinkedList<>();
                returnData = getTickerData(buildAPIURL(fromDate,toDate,symbol));
                //System.out.println(returnData.size());

            } catch(Exception ex) {
                ex.printStackTrace();
            }
            i++;
            //System.out.println("Erhöhen um "+(160-returnData.size())/2+" auf "+i);
        }*/
        String[] data = new String[returnData.size()];
        System.out.println(returnData.size());
        sizes.add(returnData.size());
        int maxSize = returnData.size();
        for(int j = 0;j<maxSize;j++){
            data[j] = returnData.pop();
            //System.out.println(data[j]);
            //System.out.println(j);
        }

        return data;
    }

    static public LinkedList<String> getTickerData(String apiURL){
        LinkedList<String> returnData = new LinkedList<String>();
        try{
            URL url = new URL(apiURL);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String strTemp = "";
            while(null!=(strTemp=br.readLine())){
                JSONObject obj = new JSONObject(strTemp);
                //JSONObject symbol_f = obj.getJSONObject("query").getJSONObject("results").getJSONArray("quote").getJSONObject(0);
                JSONArray data = obj.getJSONObject("query").getJSONObject("results").getJSONArray("quote");
                for(int i = data.length()-1;i>=0;i--){
                    //System.out.println(data.getJSONObject(i).getString("Date")+" "+data.getJSONObject(i).getString("Close"));
                    returnData.add(data.getJSONObject(i).getString("Date"));
                    returnData.add(data.getJSONObject(i).getString("Close"));
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("Company does not exist in Yahoo! Finance. Please check manually.");
            System.out.println("API URL: "+apiURL);
            e.printStackTrace();
            System.exit(-1);

        }
        return returnData;
    }

    public static String buildAPIURL(String fromDate, String toDate, String symbol){
        String dataURL = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22";
        dataURL += symbol;
        dataURL += "%22%20and%20startDate%20%3D%20%22";
        dataURL += fromDate+"%22%20and%20endDate%20%3D%20%22";
        dataURL += toDate+"%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
        //System.out.println(dataURL);
        return dataURL;
    }

    public static Date subtractWeekdays(Date d, int num) {
        int count = 0;
        Calendar c = Calendar.getInstance();
        c.setTime(d);

        do {
            c.add(Calendar.DAY_OF_YEAR, -1);
            if(isWeekday(c.get(Calendar.DAY_OF_WEEK))) {
                ++count;
            }
        } while(count < num);

        return c.getTime();
    }
    public static Date addWeekdays(Date d, int num) {
        int count = 0;
        Calendar c = Calendar.getInstance();
        c.setTime(d);

        do {
            c.add(Calendar.DAY_OF_YEAR, 1);
            if(isWeekday(c.get(Calendar.DAY_OF_WEEK))) {
                ++count;
            }
        } while(count < num);

        return c.getTime();
    }

    public static boolean isWeekday(int dayOfWeek) {
        return ((dayOfWeek != Calendar.SATURDAY) && (dayOfWeek != Calendar.SUNDAY));
    }


    static public void writeToFile(String text,String fileName) throws IOException{
        PrintWriter pw = new PrintWriter(new FileWriter(fileName,true));
        pw.print(text);
        pw.println();
        pw.close();

    }
}