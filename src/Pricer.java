import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class Pricer {
	
	//logBookAsk for maintaining Ask messages, logBookBid for maintaining Bid messages
	public static List<logItem> logBookAsk = new ArrayList<logItem>();
	public static List<logItem> logBookBid= new ArrayList<logItem>();
	
	//constants
    public static String bid = "B";
    public static String ask = "S";
    public static String add = "A";
    public static String reduce = "R";

    //initial values for logbook
    public static double income = 0; 
    public static double expense = 0;
    public static int targetSize;

	private static int totalAsks;
	
	public static int getTotalAsks(){
		return totalAsks;
	}
	
	public static void setTotalAsks(int value){
		if(value<0) 
			totalAsks = 0;
		else
		totalAsks = value;
	}
	
	public static int totalBids;
	
	public static int getTotalBids(){
		return totalBids;
	}
	
	public static void setTotalBids(int value){
		if(value<0)
			totalBids = 0;
		else
			totalBids = value;
	}
	
	public static StringBuilder output = new StringBuilder();
	

	public static void main(String[] args){
		
		// TODO Auto-generated method stub
		targetSize = Integer .valueOf(args[0]);
		
		System.out.println("The selected Target size is "+targetSize);
		System.out.println("Welcome to Pricer");
		System.out.println("Please enter the input file name");
		Scanner fileName = new Scanner(System.in);
		String file = fileName.nextLine();
		try{
		BufferedReader in  = new BufferedReader(new FileReader(file));
    		String message;
    	
		while ((message=in.readLine())!=null)
        {
                String[] fields = message.split(" ");
                if (fields.length != 0)
                {	
                    if (fields[1].equals(add))
                        addOrder(fields);
                    else if (fields[1].equals(reduce)){
                        reduceOrder(fields);
                    }
                    else
                        System.out.println("Log message format not recognized");

                }
         }
    	 
		in.close();
		BufferedWriter bwr = new BufferedWriter(new FileWriter(new File("output.txt")));
		bwr.write(output.toString());
		bwr.flush();
		bwr.close();
		fileName.close();
		} catch (FileNotFoundException e) {
			System.out.println("Please provide the correct input file name");
			System.exit(0);
		  }
		  catch (Exception ex){
		  
		  }
		System.out.println("The output is stored in output.txt");
		System.out.println("Exiting from the Pricer");		 
    }

	//For adding the message in the list
    public static void addOrder(String[] fields) throws Exception
    {
        if (fields.length != 6) 
            throw new Exception("Add Order Message format not recognized");
        
        if (fields[3].equals(bid))
        {
            insertItemAscending(logBookBid,new logItem(fields[0], fields[2], Double.parseDouble(fields[4]), Integer.parseInt(fields[5])));
            
            

            totalBids += Integer.parseInt(fields[5]);

            if(totalBids >= targetSize) 
                calculateIncome(fields[0]);

        }
        else if (fields[3].equals(ask))
        {
        	insertItemDescending(logBookAsk,new logItem(fields[0], fields[2], Double.parseDouble(fields[4]), Integer.parseInt(fields[5])));
            

            totalAsks += Integer.parseInt(fields[5]);

            if (totalAsks >= targetSize)
                calculateExpense(fields[0]);

        }
        else
        {
            System.out.println("Side not recognized");
        }
                        
    }
	
	public static void reduceOrder(String[] fields) throws Exception
    {
        if (fields.length != 4)
            throw new Exception("Add Order Message format not recognized");

        boolean isOrderFound = false;
        for (logItem item: logBookBid)
        {	
            if (item.orderID.equals(fields[2]))
            {
                isOrderFound = true;
                item.size -= Integer.parseInt(fields[3]);

                if (item.size <= 0)
                    logBookBid.remove(item);

                totalBids -= Integer.parseInt(fields[3]);

                if (totalBids >= targetSize)
                    calculateIncome(fields[0]);
                else if (totalBids < targetSize && income != 0)
                {
                    income = 0;
                    output.append(fields[0]+ " "+ ask +" NA");
                    output.append(System.lineSeparator());
                }
                break;
            }
        }
        if (!isOrderFound)
            for (logItem item: logBookAsk)
            {	
                if (item.orderID.equals(fields[2]))
                {
                    isOrderFound = true;
                    item.size -= Integer.parseInt(fields[3]);

                    if (item.size <= 0)
                        logBookAsk.remove(item);

                    totalAsks -= Integer.parseInt(fields[3]);

                    if (totalAsks >= targetSize)
                        calculateExpense(fields[0]);
                    else if (totalAsks <targetSize && expense != 0)
                    {
                        expense = 0;
                        output.append(fields[0]+ " "+ bid +" NA");
                        output.append(System.lineSeparator());
                    }

                    break;
                }
            }

        if (!isOrderFound)
            System.out.println("OrderID not found");
    }

	public static void calculateIncome(String timeStamp)
    {
        double tempIncome =0;
        int remainingSize = targetSize;

        for (logItem item: logBookBid)
        {
            if (remainingSize <= item.size)
            {
                tempIncome += (remainingSize * item.price);
                break;
            }
            else 
            {
                tempIncome += (item.size * item.price);
                remainingSize -= item.size; 
            }
        }

        if (tempIncome != income)
        {
            income = tempIncome;
            output.append(timeStamp+ " "+ ask + " "+ income);
            output.append(System.lineSeparator());
        }
    }
    //@SuppressWarnings({ "rawtypes" })
	public static void calculateExpense(String timeStamp)
    {
        double tempExpense = 0;
        int remainingSize = targetSize;

        for (logItem item: logBookAsk)
        {
            if (remainingSize <= item.size)
            {
                tempExpense += (remainingSize * item.price);
                break;
            }
            else
            {
                tempExpense += (item.size * item.price);
                remainingSize -= item.size;
            }
        }

        if (tempExpense != expense)
        {
            expense = tempExpense;
            output.append(timeStamp+ " "+ bid + " " + expense);
            output.append(System.lineSeparator());
        }
    }
	
	public static void insertItemAscending(List<logItem> list, logItem message)
    {
        boolean isAdded = false;

        for (int i = 0; i < list.size(); i++)
        {
            if (message.price > list.get(i).price)
            {
                list.add(i, message);
                isAdded = true;
                break;
            }
        }

        if (!isAdded)
            list.add(message);  
        
    }

    public static void insertItemDescending(List<logItem> list, logItem message)
    {

        boolean isAdded = false;

        for (int i = 0; i < list.size(); i++)
        {
            if (message.price < list.get(i).price)
            {
                list.add(i, message);
                isAdded = true;
                break;
            }
        }

        if (!isAdded)
            list.add(message);  

    }
   
}
   
    class logItem{
		public String timestamp;
        public String orderID;
        public double price; 
        public int size;
        public logItem(String timestamp, String orderID, double price, int size){
        	this.timestamp = timestamp;
        	this.orderID = orderID;
        	this.price = price;
        	this.size = size;
        }
	}


		

	



