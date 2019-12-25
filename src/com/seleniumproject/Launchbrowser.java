package com.seleniumproject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.gargoylesoftware.htmlunit.BrowserVersion;

public class Launchbrowser {

	private final static boolean isWindows  =System.getProperty("os.name").contains("Windows");
	private final static  String CHROMEDRIVER = isWindows ?  "chromedriver.exe" :"geckodriver"; 
	public static WebDriver driver  = driverCreator();
	private static WebDriverWait wait = new WebDriverWait(driver, 30);
	static boolean ferias = false;
	static boolean compensaçao = false;
	protected static Set<String> compensaSet;
	protected static Set<String> feriasSet;
	public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {
		//final String CHROMEDRIVER = isWindows ?  "chromedriver.exe" :"geckodriver"; 
		System.out.println(System.getProperty("os.name"));
		Scanner sc = new Scanner(System.in);
		Calendar today = Calendar.getInstance();
		int yearInt = today.get(Calendar.YEAR);
		SimpleDateFormat formatMes = new SimpleDateFormat("MMMM"); 
		String mes = formatMes.format(today.getTime());

		if(	!(new File("."+File.separator+"driver"+File.separator+CHROMEDRIVER).exists())) {
			System.out.println("chromedriver dont exist");
			configDriver();
			
		}
		
		
		menu(sc);
		
		
		driver.manage().timeouts().implicitlyWait(40, TimeUnit.SECONDS);
		driver.navigate().to("https://rm.aubay.pt");
		driver.manage().window().maximize();

		String title  = driver.getTitle();
		System.out.println(title);

		InputStream impP = Launchbrowser.class.getResourceAsStream("/prop.properties");
		Properties prop = new Properties();
		prop.load(impP);
		String pass ="";

		if(prop.getProperty("user").equalsIgnoreCase("RRusso")) {
			pass = new String (org.apache.commons.codec.binary.Base64.decodeBase64(prop.getProperty("password").getBytes()));

		}else {
			pass = prop.getProperty("password");
		}
		driver.findElement(By.id("Username")).sendKeys(prop.getProperty("user"));
		driver.findElement(By.id("Password")).sendKeys(pass);

		driver.findElement(By.xpath("//*[@id=\"login\"]/div/div[3]/div/input")).click();
		Thread.sleep(3000);

		WebElement container = driver.findElement(By.xpath("//*[@id=\"container\"]/div[5]/div/form/div[2]/div/table/tbody/tr[1]/td[3]")) ;
		//System.out.println(container.getText());
		boolean alreadyAdded = container.getText().trim().equalsIgnoreCase(mes);
		
		System.out.println(alreadyAdded ? "Já tem uma folha para este Mes "+mes : "Criada nova folha " +mes );
		
		String state =driver.findElement(By.xpath("//*[@id=\"container\"]/div[5]/div/form/div[2]/div/table/tbody/tr[1]/td[5]")).getText().trim();
		
		if (alreadyAdded && state.equalsIgnoreCase("SUBMETIDO")) {
			List<WebElement> list=	driver.findElement(By.cssSelector(".grid-row ")).findElements(By.cssSelector(".grid-cell"));

			System.out.println(" Já submetido \n");
			for (WebElement webElement : list) {

				System.out.print(" | "+webElement.getText());
			}

			
			
			driver.close();
			 return ;
		}else if(alreadyAdded && state.equalsIgnoreCase("GUARDADO")) {
			List<WebElement> list=	driver.findElement(By.cssSelector(".grid-row ")).findElements(By.cssSelector(".grid-cell"));

			System.out.println(" Já Guardado \n");
			for (WebElement webElement : list) {

				System.out.print(" | "+webElement.getText()) ;
			}
			boolean menuGuardado =  false;
			do {
				System.out.println("\n");
				System.out.println("1) Update ");
				System.out.println("2) Submeter ");
				System.out.println("3) Sair ");
				
				String choice = sc.next();
				switch (choice.trim()) {
				case "1":
					System.out.println("Update");
					menuGuardado=true;
					break;
				case "2":
					driver.findElement(By.cssSelector("#container > div.container.body-content > div > form > div.grid-mvc > div > table > tbody > tr:nth-child(1) > td.grid-cell.hidden-xs > b > a > img")).click();
					Thread.sleep(5000);
					WebElement submit = driver.findElement(By.cssSelector("#submitTimesheetForm"));
					submit.click();
					System.out.println("Submetido");
					Thread.sleep(30000);
					driver.close();
					return;
				case "3":
					System.out.println("Sair");
					driver.close();
					return;
					
				default:
					System.out.println("Valor Invalido");
					break;
				}
				
			} while (!menuGuardado);
			
			
		}

		if (!alreadyAdded) {


			Select year =  new Select(driver.findElement(ByCssSelector.cssSelector("#Year")));
			year.selectByVisibleText(Integer.toString(yearInt));

			Select month = new Select(driver.findElement(By.cssSelector("#Month")));
			month.selectByValue(Integer.toString(today.get(Calendar.MONTH)+1));

			driver.findElement(By.cssSelector("#container > div.container.body-content > div > form > div.form-data-container > div.divBottomButtons > a")).click();
			
			Thread.sleep(10000);
			driver.findElement(By.cssSelector("#container > div.container.body-content > div > form > div.grid-mvc > div > table > tbody > tr:nth-child(1) > td.grid-cell.hidden-xs > b > a > img")).click();

		}else {
			driver.findElement(By.cssSelector("#container > div.container.body-content > div > form > div.grid-mvc > div > table > tbody > tr:nth-child(1) > td.grid-cell.hidden-xs > b > a > img")).click();

		}
		Select client = new Select(driver.findElement(By.cssSelector("#ClientId")));
		client.selectByVisibleText("ACCENTURE");

		Select projecto = new Select(driver.findElement(By.cssSelector("#TariffId")));
		projecto.selectByVisibleText("Accenture");
		List<Integer> feriasFinal = new ArrayList<>();
		List<Integer> gestaoFinal = new ArrayList<>();


		List<WebElement> tableMes = driver.findElements(By.xpath("//*[@class=\"cellDataTextBox \"]"));
		boolean once2 =false;
		for (WebElement webElement : tableMes) {
			//System.out.println(webElement.findElement(By.id("Day_Day")).getAttribute("value"));

			WebElement parent = webElement.findElement(By.xpath("../.."));
			webElement.clear();
			String day =parent.findElement(By.id("Day_Day")).getAttribute("value");
			if ( 
					!( (feriasSet!=null &&feriasSet.contains(day))|| (compensaSet!=null && compensaSet.contains(day) ) ) ) {

				if(!once2) {
					System.out.print("Dia normal 8h adicionado "+day);
					once2 =true;
				}

				else System.out.print(", "+day);
				webElement.clear();
				webElement.sendKeys("8");
			}

		}

		if (ferias) {

			wait(driver.findElement(By.id("load-partial"))).click();

			driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
			System.out.println("\n loading");
			
			WebElement feriasWeb = null;
			List<WebElement> feriasTable = driver.findElements(By.cssSelector(".timesheetNonBillableRow #ActivityDescription"));
			//wait.until(ExpectedConditions.visibilityOfAllElements(feriasTable));
			for (WebElement webElement : feriasTable) {

				//					
				if(webElement.getAttribute("value").equalsIgnoreCase("Férias")) {
					feriasWeb = webElement.findElement(By.xpath("../.."));
					System.out.println("Ferias WebElement Found");
					break;


				}

			}
			if (feriasWeb!=null) {
				boolean once =false;
				List<WebElement> feriasWebDay = feriasWeb.findElements(By.cssSelector(".nonBillableData .day .cellDataTextBox "));
				for (WebElement webElement : feriasWebDay) {
					WebElement webElement2 = webElement.findElement(By.xpath("../.."));

					String day = webElement2.findElement(By.id("Day_Day")).getAttribute("value");

					if (feriasSet.contains(day)) {
						if (!once) {
							System.out.print("Adicionado dia de ferias "+day);
							once = true;
						}else {
							System.out.print(", "+day);
						}

						try {
							feriasFinal.add(Integer.parseInt(day));
						} catch (NumberFormatException e) {
							
							e.printStackTrace();
						}
						webElement.clear();
						webElement.sendKeys("8");
					}
				} 
			}

		}
		if (compensaçao) {
			driver.findElement(By.id("load-partial")).click();
			Thread.sleep(10000);System.out.println("\n Loading");
			WebElement gestaoElement = null;
			List<WebElement> gestaoTable = driver.findElements(By.cssSelector(".timesheetNonBillableRow #ActivityDescription"));
			for (WebElement webElement : gestaoTable) {

				if(webElement.getAttribute("value").equalsIgnoreCase("Gestão")) {
					gestaoElement = webElement.findElement(By.xpath("../.."));
					System.out.println("Gestão WebElement Found");
					break;


				}

			}
			if (gestaoElement!=null) {
				List<WebElement> gestaoWebDay = gestaoElement.findElements(By.cssSelector(".nonBillableData .day .cellDataTextBox "));
				boolean once3 = false;
				for (WebElement webElement : gestaoWebDay) {
					WebElement webElement2 = webElement.findElement(By.xpath("../.."));

					String day = webElement2.findElement(By.id("Day_Day")).getAttribute("value");
					//System.out.println(day);
					if (compensaSet.contains(day)) {
						if(!once3) {
							System.out.print("Compensaçao day "+day);
							once3=true;
						}
						else {
							System.out.print(", "+day);
						}
						try {
							gestaoFinal.add(Integer.parseInt(day));
						} catch (NumberFormatException e) {

							e.printStackTrace();
						}
						webElement.clear();
						webElement.sendKeys("8");
					}
				} 
			}

		}

		if(!feriasFinal.isEmpty() || !gestaoFinal.isEmpty()) {
			WebElement obser  = driver.findElement(By.cssSelector(".cellTimesheetFooterObservations "));
			Collections.sort(feriasFinal);

			String periodS = period( (!feriasFinal.isEmpty()) ? feriasFinal: gestaoFinal);

			String observations ="Dias de "+ ((ferias)?"férias ":"compensação "  ) +periodS ; 
			System.out.println("Added Observations "+observations);

			obser.clear();
			obser.sendKeys(observations);
		}

//		if(isWindows)
//			takesScreenshot(today);

		try
		{
			sc.reset();
			System.out.println("\n save ?");

			boolean endMenu = false;
			do {
				System.out.println("1) Submeter ");
				System.out.println("2) Salvar ");
				System.out.println("3) Sair ");
				String sub = sc.next();

				switch (sub.trim()) {
				case "1":
					WebElement submit = driver.findElement(By.cssSelector(".submitTimesheetForm"));
					submit.click();
					System.out.println("Submetido");
					endMenu = true;
					break;

				case "2":
					WebElement save = driver.findElement(By.xpath("//*[@id=\"saveTimesheetForm\"]"));
					
					WebElement savebtn = wait(save);
					savebtn.click();
					System.out.println("Guardado");
					endMenu = true;
					break;

				case "3":
					driver.close();
					driver.quit();
					return;
					
				default :
					System.out.println("Numero Invalido");
					break;
				}


			} while (!endMenu);

		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			sc.close();
		}
		//Thread.sleep(30000);
		driver.close();

	}


	private static WebElement wait(WebElement save) throws InterruptedException {
		
		//wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"saveTimesheetForm\"]")));
		WebElement savebtn = wait.until(ExpectedConditions.elementToBeClickable(save));
	//	driver.manage().timeouts().wait(30);
		return savebtn;
	}


	private static WebDriver driverCreator() {

		String dir	=Launchbrowser.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		String dirParent = new File(dir).getParent();
		
		
		if(isWindows) {
			System.setProperty("webdriver.chrome.driver", dirParent+File.separator+"driver"+File.separator+CHROMEDRIVER);
			return  new ChromeDriver();
					
					//new HtmlUnitDriver(BrowserVersion.INTERNET_EXPLORER,false);
		}else {
//			 FirefoxBinary firefoxBinary = new FirefoxBinary();
//			 firefoxBinary.addCommandLineOptions("--headless");
//			 firefoxBinary.addCommandLineOptions("--no-sandbox");
//			 System.setProperty("webdriver.gecko.driver",  dirParent+File.separator+"driver"+File.separator+CHROMEDRIVER);
//			 FirefoxOptions firefoxOptions = new FirefoxOptions();
//			 firefoxOptions.setBinary(firefoxBinary);
			System.out.println("Chrome");
			 return new HtmlUnitDriver(BrowserVersion.CHROME);

		}
		
		
	}


	private static void configDriver() throws IOException{
		InputStream imput = Launchbrowser.class.getResourceAsStream("/chromedriver.exe");
	
		String dirTxt = Launchbrowser.class.getProtectionDomain().getCodeSource().getLocation().getFile();

	
		String dirTxtPar =  new File(dirTxt).getParent();
		new File(dirTxtPar+ File.separator +   "driver").mkdirs();
		Files.copy( imput, Paths.get(dirTxtPar+File.separator+"driver" +File.separator+CHROMEDRIVER),StandardCopyOption.REPLACE_EXISTING);
		System.out.println("driver Criado");
	}


	private static void takesScreenshot(Calendar today) throws WebDriverException, IOException, URISyntaxException {
		TakesScreenshot scre = (TakesScreenshot) driver;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh_mm");
		System.out.println("\n taking sCreenshot");
		File image = scre.getScreenshotAs(OutputType.FILE);
		String dir2 = Launchbrowser.class.getProtectionDomain().getCodeSource().getLocation()
				.toURI().getPath();
		
		String parantDir =new File(dir2).getParent();
		
	
		String dirFinal =parantDir+"\\Screenshot";
		new File(dirFinal).mkdirs();
		com.google.common.io.Files.copy(image, new File(dirFinal +"\\Screenshot_" +dateFormat.format(today.getTime())+".bmp"));

	}


	private static String period(List<Integer> feriasFinal) {
		System.out.println(feriasFinal);
		int begin =0;
		int end;
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < feriasFinal.size(); i++) {
			if (begin==0) {
				begin=feriasFinal.get(i);
				if(i==feriasFinal.size()-1) {
					str.append((str.length()>0) ? "e "+begin:" "+begin);
					continue;
				}


			}
			if((i+1<feriasFinal.size()) && feriasFinal.get(i)+1!=feriasFinal.get(i+1)) {
				end= feriasFinal.get(i);
				str.append(begin + " a "+ end +", ")  ;
				begin = 0;
			}else if(i==feriasFinal.size()-1) {
				end= feriasFinal.get(i);
				str.append("e "+begin + " a "+ end +". ")  ;
			}



		}
		System.out.println("Periodo Ferias "+str);
		return str.toString();
	}


	private static void menu(Scanner sc) {
		try 

		{

			boolean menu = true;
			menu:do {
				System.out.println("Ferias ou dias de Compensação ?");
				System.out.println("1) Ferias");
				System.out.println("2) Compensação");

				String response = removeIlegalChar(sc.next());
				
				if(checkForExit(response)) {
					menu=false;
				}

				switch (response.trim()) {

				case "1":
					ferias=true;
					do {
						System.out.println("Dias separados por virgulas ");
						sc.reset();
						String imput= sc.nextLine();
						if(imput.contains(",")) {
							String dias = removeIlegalChar(imput);
							if(dias.equalsIgnoreCase("n")|| dias.equalsIgnoreCase("nao")||dias.equalsIgnoreCase("não")) {
								ferias =false;
								break menu;
							}	



							feriasSet = removeLetters(dias.split(","));


						}else {
							feriasSet =	checkperid(imput);
						}

						System.out.println("Dias de ferias " + feriasSet);
						menu = feriasSet.isEmpty();

					} while (menu);

					break;
				case "2":
					compensaçao=true;
					do {
						System.out.println("Dias separados por virgulas ");
						String dias = removeIlegalChar(sc.nextLine());
						if(dias.equalsIgnoreCase("n")|| dias.equalsIgnoreCase("nao")||dias.equalsIgnoreCase("não")) {
							compensaçao =false;
							break menu;
						}	

						compensaSet = removeLetters(dias.split(","));
						System.out.println(compensaSet);
						menu = compensaSet.isEmpty();
					} while (menu);

					break;
					default:
						if(!checkForExit(response))
							System.out.println("Numero invalido");
						break;
				}
				
				sc.reset();
			} while (menu);
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	private static HashSet<String> checkperid(String imput) {
		System.out.println(imput);
		//14 a 18 e 19 a 29
		LinkedHashSet<String> set = new LinkedHashSet<>();
		String [] split;
		if (imput.contains("e")) {
			split = imput.split("e");

		}else {
			split = new String [] {
					imput
			};

		}

		for (int i = 0; i < split.length; i++) {
			String [] splitA = removeIlegalChar(split[i]).split("a");
			List<Integer> list = new ArrayList<>();
			for (String string : splitA) {
				String str = removeLetters(string);
				if(!str.equals(""))
					list.add(Integer.parseInt(str));
			}
			Collections.sort(list);

			if (!list.isEmpty()) {
				for (int j = list.get(0); j <= list.get(list.size() - 1); j++) {

					set.add(Integer.toString(j));
				} 
			}

		}

		return new HashSet<>(set);

	}


	private static String removeLetters(String string) {

		char [] arr = string.toCharArray();
		String result ="";
		for (int i = 0; i < arr.length; i++) {
			if(Character.isDigit(arr[i])){
				result+=arr[i];
			}
		}

		if (result!="" && Integer.parseInt(result)>0 && Integer.parseInt(result)<=31){
			return result;
		}else {
			return "";
		}


	}


	private static HashSet<String> removeLetters(String[] split) {
		HashSet<String> set = new HashSet<>();
		for (String string : split) {
			char [] arr = string.toCharArray();
			String result ="";
			for (int i = 0; i < arr.length; i++) {
				if(Character.isDigit(arr[i])){
					result+=arr[i];
				}
			}

			if (result!="" && Integer.parseInt(result)>0 && Integer.parseInt(result)<=31){
				set.add(result);
			}

		}


		return set;
	}

	private static String removeIlegalChar(String next) {
		char[] arrChar = {';','.',':','.' ,'<','>' 	
		};
		//System.out.println(next);
		for (int i = 0; i < arrChar.length; i++) {

			next = next.replace(arrChar[i], ' ');

		}

		String result =next.replaceAll(" ", "");

		return result.trim();
	}
	private static boolean checkForExit(String str) {
		List<String> negativeList = Arrays.asList("não","n", "nao", "sair", "quit", "exit");
		boolean isNegative = false;
		
		Iterator<String>	it =negativeList.iterator();
		
		while (it.hasNext()) {
			String string =  it.next();
			if(string.equalsIgnoreCase(str.trim())) {
				isNegative=true;
				break;
			}
		}
		
		return isNegative;
	}

}
