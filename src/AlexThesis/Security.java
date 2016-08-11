package AlexThesis;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Robert Maier on 22.07.2016.
 */
public class Security {

    private String acquirerISIN;
    private int dealNumber;
    private String dealStatus;
    private String stockIndex;

    private int observationPeriodLength;
    private int eventWindowLength;

    //Variables for Rumor date
    private LinkedList<Double> rumorStockReturn;
    private LinkedList<Double> rumorIndexReturn;
    private String rumorDate;
    private LinkedList<Double> rumorAbnormalReturns;
    private LinkedList<Double> rumorAbnormalReturnsSignificance;
    private Double cumulativeARRumor;


    //Variables for Withdrawal date
    private LinkedList<Double> withdrawalStockReturn;
    private LinkedList<Double> withdrawalIndexReturn;
    private LinkedList<Double> withdrawalAbnormalReturns;
    private LinkedList<Double> withdrawalAbnormalReturnsSignificance;
    private String withdrawalDate;
    private Double cumulativeARWithdrawal;


    public Security(String[] data, int observationPeriodLength, int eventWindowLength, LinkedList<Integer> windowSizes)
    {
        //Data comes as endless string-array, but every part (rumor stock/index price, withdrawal stock/index price) is of same length
        //Before data starts, we have: Deal Number, Deal Status, Acquirer ISIN, Status, Rumor Date, Withdrawal Date, Index
        this.observationPeriodLength = observationPeriodLength;
        this.eventWindowLength = eventWindowLength;
        this.rumorDate = data[4];
        this.withdrawalDate = data[5];
        this.dealNumber = Integer.parseInt(data[0]);
        this.dealStatus = data[1];
        this.acquirerISIN = data[2];
        this.stockIndex = data[6];

        int start = 7;
        rumorStockReturn = calculateReturns(fillHashMap(data,start,(windowSizes.get(0))));
        //System.out.println(rumorStockReturn);
        start += windowSizes.get(0);
        rumorIndexReturn = calculateReturns(fillHashMap(data,start,windowSizes.get(1)));
        //System.out.println(rumorIndexReturn);
        start += windowSizes.get(1);
        withdrawalStockReturn = calculateReturns(correctObservationWindow(fillHashMap(data,start,windowSizes.get(2))));
        start += windowSizes.get(2);
        withdrawalIndexReturn = calculateReturns(correctObservationWindow(fillHashMap(data,start,windowSizes.get(3))));
        //System.out.println(windowSizes);
        //System.out.println(rumorStockReturn.size() + " - " + rumorIndexReturn.size());
        rumorAbnormalReturns = calculateAR((LinkedList<Double>)rumorStockReturn.clone(),(LinkedList<Double>)rumorIndexReturn.clone(),
                createRegressionModel((LinkedList<Double>)rumorStockReturn.clone(),(LinkedList<Double>)rumorIndexReturn.clone()));
        //System.out.println(rumorAbnormalReturns);
        SimpleRegression model = createRegressionModel((LinkedList<Double>)withdrawalStockReturn.clone(),(LinkedList<Double>)withdrawalIndexReturn.clone());
        //System.out.println("Regression Model 1: Intercept - "+model.getIntercept()+", Slope - "+model.getSlope());
        //System.out.println("Length withdrawalStockReturn: "+withdrawalStockReturn.size()+", Length withdrawalIndexReturn: "+withdrawalIndexReturn.size());
        withdrawalAbnormalReturns = calculateAR((LinkedList<Double>)withdrawalStockReturn.clone(),(LinkedList<Double>)withdrawalIndexReturn.clone(),
                createRegressionModel((LinkedList<Double>)withdrawalStockReturn.clone(),(LinkedList<Double>)withdrawalIndexReturn.clone()));
        //model = createRegressionModel((LinkedList<Double>)withdrawalStockReturn.clone(),(LinkedList<Double>)withdrawalIndexReturn.clone());

        //System.out.println("Regression Model 2: Intercept - "+model.getIntercept()+", Slope - "+model.getSlope());
        cumulativeARWithdrawal = 0.0;
        cumulativeARRumor = 0.0;
        for(Double sum : rumorAbnormalReturns){
            cumulativeARRumor+=sum;
        }
        for(Double sum : withdrawalAbnormalReturns){
            cumulativeARWithdrawal +=sum;
        }

        this.rumorAbnormalReturnsSignificance = calculateSignificance((LinkedList<Double>)rumorAbnormalReturns.clone(),createRegressionModel((LinkedList<Double>)rumorStockReturn.clone(),(LinkedList<Double>)rumorIndexReturn.clone()));
        //model = createRegressionModel((LinkedList<Double>)rumorStockReturn.clone(),(LinkedList<Double>)rumorIndexReturn.clone());
        this.withdrawalAbnormalReturnsSignificance = calculateSignificance((LinkedList<Double>)withdrawalAbnormalReturns.clone(),
                createRegressionModel((LinkedList<Double>)withdrawalStockReturn.clone(),(LinkedList<Double>)withdrawalIndexReturn.clone()));
    }

    public String toString(){
        String temp = this.dealNumber+","+this.acquirerISIN+","+this.dealStatus+","+this.stockIndex+","+this.rumorDate+","+this.withdrawalDate+", rumor Abnormal Returns: ";

        while(this.rumorAbnormalReturns.size()>0){
            temp = temp+round(this.rumorAbnormalReturns.pop())+",";
        }
        temp += "Cumulative rumor ARs: " + cumulativeARRumor+", significance: ";
        while(this.rumorAbnormalReturnsSignificance.size()>0) {
            temp = temp + round(this.rumorAbnormalReturnsSignificance.pop()) + ",";
        }
        temp += "withdrawal Abnormal Returns: ";
        while(this.withdrawalAbnormalReturns.size()>0){
            temp = temp+round(this.withdrawalAbnormalReturns.pop())+",";
        }
        temp+= "Cumulative rumor ARs: " + cumulativeARWithdrawal+", significance: ";
        while(this.withdrawalAbnormalReturnsSignificance.size()>0){
            temp = temp+round(this.withdrawalAbnormalReturnsSignificance.pop())+",";
        }
        return temp;
    }

    private static double round(double d){
        double temp = d*1000;
        temp = Math.round(temp);
        return temp/1000;
    }

    private HashMap<String,Double> fillHashMap (String[] data, int start, int length) {
        HashMap<String, Double> toBeFilled = new HashMap<>();
        for (int i = 0; i < length; i += 2) {
            toBeFilled.put(data[start + i], Double.parseDouble(data[start + i + 1]));
        }
        return toBeFilled;
    }

    private SimpleRegression createRegressionModel(LinkedList<Double> stock, LinkedList<Double> index){
        SimpleRegression model = new SimpleRegression();
        //System.out.println(index.peek()+ " " + stock.peek());
        if(index.size()>=stock.size()){
            while(stock.size()>(this.eventWindowLength*2+1)) model.addData(index.pop(), stock.pop());
        }else{
        while(index.size()>(this.eventWindowLength*2+1)) model.addData(index.pop(), stock.pop());}
        return model;
    }

    private LinkedList<Double> calculateAR(LinkedList<Double> stock, LinkedList<Double> index, SimpleRegression model){
        LinkedList<Double> temp = new LinkedList<>();
        if(stock.size()<=index.size()){
            for(int i = (stock.size()-(this.eventWindowLength*2+1));i<stock.size();i++){
                double t = stock.get(i)-(model.getIntercept()+model.getSlope()*index.get(i));
                temp.add(t);
        }}
        else{
            for(int i = (index.size()-(this.eventWindowLength*2+1));i<index.size();i++){
                double t = stock.get(i)-(model.getIntercept()+model.getSlope()*index.get(i));
                temp.add(t);
        }}
        return temp;
    }

    private LinkedList<Double> calculateSignificance(LinkedList<Double> abnormalReturns, SimpleRegression model){
        LinkedList<Double> temp = new LinkedList<Double>();
        while(abnormalReturns.size()>0){
            temp.add(abnormalReturns.pop()/Math.sqrt(model.getSumSquaredErrors()/(model.getN()-2.0)));}
        return temp;
    }

    private HashMap<String,Double> correctObservationWindow (HashMap<String,Double> data){
        String tempDate = "";
        //System.out.println("Length before correction: "+data.size());
        //System.out.println(data.keySet());
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date d = dateFormat.parse(this.withdrawalDate);
            tempDate    =   dateFormat.format(subtractWeekdays(d,(this.eventWindowLength*2+this.observationPeriodLength)));
            if(dateFormat.parse(tempDate).after(addWeekdays(dateFormat.parse(rumorDate),this.eventWindowLength))){

            }else{
                Date endeVomEventWindow = addWeekdays(dateFormat.parse(this.rumorDate),this.eventWindowLength);
                Date i = subtractWeekdays(dateFormat.parse(this.rumorDate),this.eventWindowLength);

                while(i.before(endeVomEventWindow)){
                    //System.out.print(dateFormat.format(i));
                    //System.out.println("Date to be removed: "+dateFormat.format(i)+", value to be removed: "+data.get(dateFormat.format(i)));
                    data.remove(dateFormat.format(i));
                    i = addWeekdays(i,1);
                    //System.out.println(dateFormat.format(i));
                }

            }

        }catch(Exception e){
            e.printStackTrace();
        }
        //System.out.println("Length after correction: "+data.size());
        return data;
    }

    private boolean isWithinRange(Date testDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date startDate = subtractWeekdays(dateFormat.parse(this.rumorDate), (eventWindowLength));
            Date endDate = addWeekdays(dateFormat.parse(this.rumorDate), (eventWindowLength));
            System.out.println(dateFormat.format(startDate)+" - "+dateFormat.format(endDate));
            return !(testDate.before(startDate) || testDate.after(endDate));
        }catch (Exception e){
            e.printStackTrace();

        }

        return false;
    }

    private LinkedList<Double> calculateReturns(HashMap<String,Double> data){

        LinkedList<Double> temp = new LinkedList<>();
        String[] test = data.keySet().toArray(new String[data.keySet().size()]);
        Arrays.sort(test);

        for(int i = 1;i<test.length;i++){
            Double a = data.get(test[i-1]);
            Double b = data.get(test[i]);
            temp.add((b-a)/a);
        }
        return temp;
    }


    private Date subtractWeekdays(Date d, int num) {
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
    private Date addWeekdays(Date d, int num) {
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
    private boolean isWeekday(int dayOfWeek) {
        return ((dayOfWeek != Calendar.SATURDAY) && (dayOfWeek != Calendar.SUNDAY));
    }


}
