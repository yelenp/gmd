package gmd;

public class App {
	
	public static void main(String[] args) {
		System.out.println("Call initialize() for initializing the application.");
	}
	
	public static void initialize() {
		Indexer.indexData();
	}
}