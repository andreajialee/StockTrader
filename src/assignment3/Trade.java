package assignment3;

public class Trade extends Thread {
	int time;
	String ticker;
	int quantity;
	
	public Trade(int time, String ticker, int quantity) {
		this.time = time;
		this.ticker = ticker;
		this.quantity = quantity;
    }

	public Trade() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Trading function using locks
	 */
	public void run() {
		try {
			// Decide if we are selling or purchasing stocks based on quantity
			String eventAction = "";
			if(this.quantity >= 0) {
				eventAction = "purchase";
			}
			else {
				eventAction = "sale";
			}
			// If quantity is singular, we output "stock" instead of "stocks"
			String stock = "";
			if(this.quantity == 1 || this.quantity == -1) {
				stock = "stock";
			}
			else {
				stock = "stocks";
			}
			Thread.sleep(time * 1000);
			
			PA3.sem.get(this.ticker).acquire();
			System.out.println("[" + Utility.getZeroTimestamp() + "] Starting " + eventAction + " of " 
					+ Math.abs(this.quantity) + " " + stock + " of " + this.ticker);
			Thread.sleep(1000);
			System.out.println("[" + Utility.getZeroTimestamp() + "] Finished " + eventAction + " of " 
					+ Math.abs(this.quantity) + " " + stock + " of " + this.ticker);
		}
		catch (InterruptedException err) {
			System.out.println(err);
		}
		finally {
			PA3.sem.get(this.ticker).release();
		}
	}
}
