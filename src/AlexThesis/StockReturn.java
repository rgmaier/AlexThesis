package AlexThesis;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.LinkedList;

/**
 * Created by Robert Maier on 08.07.2016.
 */
public class StockReturn {

    private String[] rawData;
    private String acquirerISIN;
    private String dealNumber;
    private String dealStatus;

    private LinkedList<Double> StockPriceRumor; //Stockprices in Liste laden, same for index und dann returns berechnen
    private LinkedList<Double> IndexPriceRumor;
    private LinkedList<Double> StockReturnRumor;
    private LinkedList<Double> IndexReturnRumor;

    private LinkedList<Double> abnormalReturnRumor;
    private LinkedList<Double> abnormalReturnRumorSignificance;

    private LinkedList<Double> StockPriceWithdrawal; //Stockprices in Liste laden, same for index und dann returns berechnen
    private LinkedList<Double> IndexPriceWithdrawal; //121 infos
    private LinkedList<Double> StockReturnWithdrawal;
    private LinkedList<Double> IndexReturnWithdrawal;

    private LinkedList<Double> abnormalReturnWithdrawal;
    private LinkedList<Double> abnormalReturnWithdrawalSignificance;

    private SimpleRegression regressionModelRumor;
    private SimpleRegression regressionModelWithdrawal;



    public StockReturn(String[] data){
        this.rawData = data;
        this.dealNumber = data[0];
        this.acquirerISIN = data[2];
        this.dealStatus = data[4];
        this.StockPriceRumor = fillList(data,6,165);
        this.IndexPriceRumor = fillList(data,166,325); //TODO: Zahlen updaten
        this.StockPriceWithdrawal = fillList(data, 326, 485); //TODO: Zahlen updaten
        this.IndexPriceWithdrawal = fillList(data, 486, 645); //TODO: Zahlen updaten

        this.StockReturnRumor = calculateReturns(this.StockPriceRumor);
        this.IndexReturnRumor = calculateReturns(this.IndexPriceRumor);
        this.StockReturnWithdrawal = calculateReturns(this.StockPriceWithdrawal);
        this.IndexReturnWithdrawal = calculateReturns(this.IndexPriceWithdrawal);

        this.regressionModelRumor = createRegressionModel(this.StockReturnRumor,this.IndexReturnRumor);
        this.regressionModelWithdrawal = createRegressionModel(this.StockReturnWithdrawal,this.IndexReturnWithdrawal);

        this.abnormalReturnRumor = calculateAbnormalReturn(this.StockReturnRumor,this.IndexReturnRumor,this.regressionModelRumor);
        this.abnormalReturnRumorSignificance = calculateSignificance(this.abnormalReturnRumor,this.regressionModelRumor);

        this.abnormalReturnWithdrawal = calculateAbnormalReturn(this.StockReturnWithdrawal, this.IndexReturnWithdrawal, this.regressionModelWithdrawal);
        this.abnormalReturnWithdrawalSignificance = calculateSignificance(this.abnormalReturnWithdrawal,this.regressionModelWithdrawal);
    }

    private LinkedList<Double> fillList(String[] data, int beginning, int end){
        LinkedList<Double> temp = new LinkedList<Double>();
        for(int i=beginning;i<end;i+=2)
        {
            temp.add(Double.parseDouble(data[i]));
        }
        return temp;
    }

    private LinkedList<Double> calculateReturns(LinkedList<Double> stockPrice){
        LinkedList<Double> temp = new LinkedList<Double>();
        LinkedList<Double> copy = (LinkedList<Double>) stockPrice.clone();
        double a; //früherer Return
        double b; //späterer Return

        while(copy.size()>1)
        {
            a = copy.pop();
            b = copy.peek();
            temp.add((b-a)/a);
        }

        return temp;
    }

    private SimpleRegression createRegressionModel(LinkedList<Double> stock, LinkedList<Double> index){
        SimpleRegression model = new SimpleRegression();
        LinkedList<Double> a = (LinkedList<Double>) stock.clone();
        LinkedList<Double> b = (LinkedList<Double>) index.clone();
        while(b.size()>21) model.addData(b.pop(), a.pop());
        return model;
    }

    private LinkedList<Double> calculateAbnormalReturn(LinkedList<Double> stock, LinkedList<Double> index, SimpleRegression model){
        LinkedList<Double> temp = new LinkedList<Double>();

        for(int i = 58;i<79;i++){
            double t = stock.get(i)-(model.getIntercept()+model.getSlope()*index.get(i));
            temp.add(t);
        }
        return temp;
    }


    private LinkedList<Double> calculateSignificance(LinkedList<Double> abnormalReturns, SimpleRegression model){
        LinkedList<Double> temp = new LinkedList<Double>();
        LinkedList<Double> ret = (LinkedList<Double>) abnormalReturns.clone();
        while(ret.size()>0){
        temp.add(ret.pop()/Math.sqrt(model.getSumSquaredErrors()/(model.getN()-2.0)));}
        return temp;
    }

    private static double round(double d){
        double temp = d*1000;
        temp = Math.round(temp);
        return temp/1000;
    }

    @Override
    public String toString(){
        String temp = this.dealNumber+","+this.acquirerISIN+","+this.dealStatus+",";
        //temp = temp+this.regressionModelRumor.getIntercept()+","+this.regressionModelRumor.getSlope()+",";
        /*while(IndexReturnRumor.size()>0){
            temp = temp+IndexReturnRumor.pop()+",";
        }*/

        //System.out.println(abnormalReturnRumor.size());
        while(abnormalReturnRumor.size()>0){
            temp = temp+round(abnormalReturnRumor.pop())+",";
        }
        //System.out.println(abnormalReturnRumorSignificance.size());
        while(abnormalReturnRumorSignificance.size()>0){
            temp = temp+round(abnormalReturnRumorSignificance.pop())+",";
        }
        //System.out.println(abnormalReturnWithdrawal.size());
        while(abnormalReturnWithdrawal.size()>0){
            temp = temp+round(abnormalReturnWithdrawal.pop())+",";
        }
        //System.out.println(abnormalReturnWithdrawalSignificance.size());
        while(abnormalReturnWithdrawalSignificance.size()>0){
            temp = temp+round(abnormalReturnWithdrawalSignificance.pop())+",";
        }
        return temp;
    }

}
