package cwoop;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SlotMachine extends Application {
	
	//variables needed to get statistics
    private static int playCountWin = 0;
    private static int playCountLose = 0;
    private static int creditsEarnCount = 0;
    private static int totalEarnCount = 0;
    private static int totalSpendCount = 1;
    private static int Credits = 10;
    private static int crntBet = 0;
    private static boolean checkedWin = false;
    private volatile double speedControl=120;
    private static double fnlPayout=0;
    
    //Executor to run threads smoothly
    private ExecutorService executor = Executors.newScheduledThreadPool(3);
    
    private BorderPane gamePane;

    //labels of the ui
    private Label l0 = new Label();
    private Label l1 = new Label();
    private Label l2 = new Label();
    private Label header = new Label();
    private static Label lblCredit = new Label();
    private static Label lblBet = new Label();

    //all the buttons in the ui
    private static Button btnSpin = new Button("SPIN!");
    private static Button btnAddC = new Button("ADD COIN");
    private static Button btnBetO = new Button("BET ONE");
    private static Button btnBetM = new Button("BET MAX");
    private static Button btnReset = new Button("RESET");
    private static Button btnStat = new Button("STATS");
    private static Button btnSaveStat = new Button("Save to File");
    

    //reel and reelthread class objects
    private static Reel r0, r1, r2;
    private static ReelThread reel0, reel1, reel2;

 
    public static void main(String[] args) {
        Application.launch(args);
    }

    //three arrays of symbols containing randomized reel images
    private static Symbol[] symArr0, symArr1, symArr2;

    @Override
    public void start(Stage primaryStage) {
    	//main satge of the ui
        primaryStage.setTitle("Slot Machine");
        primaryStage.setMinHeight(250);
        primaryStage.setMinWidth(500);

        //ui settings
        gamePane = new BorderPane();
        gamePane.setPadding(new Insets(10, 30, 10, 30));
        gamePane.setMinHeight(250);
        gamePane.setMinWidth(500);
        gamePane.setId("back");
        
        //header
        ImageView head = new ImageView(new Image("file:src/logo.png"));
        
        //auto-resizing 
        head.fitWidthProperty().bind(gamePane.widthProperty().divide(1.7));
        head.fitHeightProperty().bind(gamePane.heightProperty().divide(4));
        header.setGraphic(head);
        
        //button area
        HBox betArea = new HBox(btnAddC, btnBetM, btnBetO, btnReset, btnStat);
        betArea.setAlignment(Pos.CENTER);
        betArea.setSpacing(10);
        
        //main ui structure
        GridPane gameReel = new GridPane();        
        gameReel.setAlignment(Pos.CENTER);
        gameReel.setGridLinesVisible(true);
        //main ui structure set-up
        BorderPane.setAlignment(gameReel, Pos.CENTER);
        gamePane.setCenter(gameReel);
        gamePane.setBottom(betArea);
        VBox top =new VBox(header,btnSpin);
        top.setAlignment(Pos.CENTER);
        gamePane.setTop(top);

        //betarea and credit area using two stackpane structures
        StackPane betPane = new StackPane(lblBet);
        StackPane creditPane = new StackPane(lblCredit);
        betPane.setMinSize(40, 40);
        betPane.setPadding(new Insets(5));
        creditPane.setMinSize(40, 40);
        creditPane.setPadding(new Insets(5));
        gamePane.setLeft(betPane);
        gamePane.setRight(creditPane);
        btnSpin.setMaxWidth(Double.MAX_VALUE);
        btnSpin.setMaxHeight(Double.MAX_VALUE);

        //setting credit and bet labels
        lblCredit.setText("Credits :" + Integer.toString(Credits));
        lblBet.setText("Bet :" + Integer.toString(crntBet));
        lblCredit.setId("stats");
        lblBet.setId("stats");

        //creation of new objects of reels
        r0 = new Reel();
        r1 = new Reel();
        r2 = new Reel();

        //GridPane inside the borderpane in order to place the reel images 
        gameReel.addRow(0, l0, l1, l2);
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setPercentWidth(50);
        RowConstraints rowCon = new RowConstraints();
        rowCon.setPercentHeight(70);
        gameReel.getColumnConstraints().addAll(column1, column2, column3);
        gameReel.getRowConstraints().addAll(rowCon);

        //Initializing reel image arrays
        symArr0 = r0.getArrSym().toArray(new Symbol[r0.getArrSym().size()]);
        symArr1 = r1.getArrSym().toArray(new Symbol[r1.getArrSym().size()]);
        symArr2 = r2.getArrSym().toArray(new Symbol[r2.getArrSym().size()]);

        //crating three threads for three reels
        reel0 = new ReelThread(symArr0, l0);
        reel1 = new ReelThread(symArr1, l1);
        reel2 = new ReelThread(symArr2, l2);

        //execution of three threads inside the thead executor
        Thread[] tArr = new Thread[3];
        tArr[0] = new Thread(reel0);
        tArr[1] = new Thread(reel1);
        tArr[2] = new Thread(reel2);
        for (Thread t : tArr) {
            t.setDaemon(true);//improving performance
            executor.execute(t);

        }
        
        
        //button actions setting
        btnSpin.setId("btnSpin");
        
        btnSpin.setOnAction((event) -> {
            if (!(reel0.spinning || reel1.spinning || reel2.spinning)) {
                if (Validate(crntBet, "spin")) {
                    checkedWin = false;
                    speedControl=payoutControl(totalSpendCount, creditsEarnCount, 0.9);
                    reel0.startR();
                    reel1.startR();
                    reel2.startR();
                    System.out.println("pause time : "+(speedControl+120));
                	totalSpendCount+=crntBet;

                }

            }

        });

        btnBetM.setId("btn");
        btnBetM.setOnAction((event) -> {
        	if(crntBet==0){
        		clickButton(3, "bet");
        	}
            
        });
        btnBetO.setId("btn");
        btnBetO.setOnAction((event) -> {
            clickButton(1, "bet");
        });
        btnAddC.setId("btn");
        btnAddC.setOnAction((event) -> {
            clickButton(1, "addCoin");
        });
        btnReset.setId("btn");
        btnReset.setOnAction((event) -> {
            clickButton(0, "reset");
        });
        btnStat.setId("btn");
        btnStat.setOnAction((ActionEvent e) -> {
            showStats();
        });

        //scene creation
        Scene scene = new Scene(gamePane, 1000, 500);
        //adding css stylings
        String css = this.getClass().getResource("style.css").toExternalForm();
        scene.getStylesheets().add(css);
        primaryStage.setScene(scene);
        
        //locking the ratio of the window
        primaryStage.minHeightProperty().bind(scene.widthProperty().divide(2));
        primaryStage.minWidthProperty().bind(scene.heightProperty().multiply(2));
        primaryStage.show();

    }

    //method to execute related action which are on button clicks
    public static void clickButton(int Increment, String s) {

        if (reel0.spinning || reel1.spinning || reel2.spinning) {
            s = "-";
        }

        switch (s) {
            case "bet":
                if (Validate(Increment, "bet")) {
                    crntBet += Increment;
                    Credits -= Increment;
                    lblCredit.setText("Credits :" + Integer.toString(Credits));
                    lblBet.setText("Bet :" + Integer.toString(crntBet));
                }
                break;

            case "addCoin":
                Credits += Increment;
                lblCredit.setText("Credits :" + Integer.toString(Credits));
                break;
            case "reset":
                Credits += crntBet;
                crntBet = 0;
                lblCredit.setText("Credits :" + Integer.toString(Credits));
                lblBet.setText("Bet :" + Integer.toString(crntBet));

                break;
            default:
        }

    }
    
    //all the validations will be through this method
    //gets the value and the validation type as arguments
    public static boolean Validate(int val, String in) {
        boolean validity = false;
        switch (in) {
            case "bet":
                if (val < Credits) {
                    validity = true;
                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("OOPS...");
                    alert.setHeaderText("Low credit !!");
                    alert.setContentText("Add more coins to continue playing");
                    alert.showAndWait();

                }
                break;
            case "spin":
                if (val != 0) {
                    validity = true;
                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("OOPS...");
                    alert.setHeaderText("Bet credits !!");
                    alert.setContentText("Add credits to bet before you spin");
                    alert.showAndWait();

                }
                break;

        }
        return validity;

    }
    
    //method to check for matching reel images
    public static void checkWin() {
        checkedWin = true;

        if (symArr0[reel0.imgIndexChoosen].checkEquals(symArr1[reel1.imgIndexChoosen])) {
            showMessage(symArr0[reel0.imgIndexChoosen].getValue());
            addRewards(0);
        } else if (symArr0[reel0.imgIndexChoosen].checkEquals(symArr2[reel2.imgIndexChoosen])) {
            showMessage(symArr0[reel0.imgIndexChoosen].getValue());
            addRewards(0);

        } else if (symArr1[reel1.imgIndexChoosen].checkEquals(symArr2[reel2.imgIndexChoosen])) {
            showMessage(symArr1[reel1.imgIndexChoosen].getValue());
            addRewards(1);

        } else {
            showMessage(-1);
            addRewards(-1);
        }

    }

    //method which shows related error messages
    public static void showMessage(int i) {
        if (i == -1) {
            Alert alertWin = new Alert(AlertType.ERROR);
            alertWin.setTitle("Bad Luck...");
            alertWin.setHeaderText("You Lose !!!");
            alertWin.setContentText("You will lose the credits you bet");
            alertWin.showAndWait();

        } else {
            String r = Integer.toString(i * crntBet);
            Alert alertWin = new Alert(AlertType.INFORMATION);
            alertWin.setTitle("Winner !!!");
            alertWin.setHeaderText("You Win !!!");
            alertWin.setContentText("Your rewards of " + r + " added to your Credits");
            alertWin.showAndWait();
        }
    }

    //add won rewards to the ui as well as to the stat variables
    public static void addRewards(int i) {
        switch (i) {
            case 0:
                int earned0=symArr0[reel0.imgIndexChoosen].getValue() * crntBet;
                Credits += earned0;
                crntBet = 0;
                lblCredit.setText("Credits :" + Integer.toString(Credits));
                lblBet.setText("Bet :" + Integer.toString(crntBet));
                playCountWin++;
                creditsEarnCount+=earned0;
                totalEarnCount+=earned0;
                break;

            case 1:
                int earned1=symArr1[reel1.imgIndexChoosen].getValue() * crntBet;
                Credits += earned1;
                crntBet = 0;
                lblCredit.setText("Credits :" + Integer.toString(Credits));
                lblBet.setText("Bet :" + Integer.toString(crntBet));
                playCountWin++;
                creditsEarnCount+=earned1;
                totalEarnCount+=earned1;

                break;

            case -1:
                int lost=crntBet;
                crntBet = 0;
                lblBet.setText("Bet :" + Integer.toString(crntBet));
                playCountLose++;
                totalEarnCount-=lost;

                break;

        }

    }

    // method which show the stat windows of the player
    public static void showStats() {
    	//equation which calculates the percentages and Average credits
        double winP = ((double) playCountWin / (playCountWin + playCountLose)) * 100;
        double loseP = ((double) playCountLose / (playCountWin + playCountLose)) * 100;
        double avgCredits = (double)creditsEarnCount/ (playCountWin + playCountLose);
        if(totalEarnCount<0){
            avgCredits = -avgCredits; 
        }
        //formating the decimal number
        DecimalFormat twoDigit = new DecimalFormat("#.00");
        DecimalFormat twoDigitSign = new DecimalFormat("+#.00;-#");

        Stage stage = new Stage();
        PieChart pieChart = new PieChart();
        pieChart.setData(getChart(winP, loseP));
        stage.setTitle("PieChart");
        String[] fileData=getSaveData(twoDigit.format(winP), twoDigit.format(loseP), twoDigitSign.format(avgCredits));

        //save button
        btnSaveStat.setOnAction((e) -> {
            Alert saved = new Alert(AlertType.INFORMATION);
            saved.setTitle("Save Stats");
            saved.setHeaderText("Saved");
            saved.setContentText("Press OK to Save");
            saved.showAndWait();
            saveToFile(fileData);
        });
        //ui structuring of the stat window
        VBox root = new VBox();
        HBox stats = new HBox();
        Label winPer, losePer,avgCr;
        winPer = new Label("Win Percentage :" + twoDigit.format(winP));
        losePer = new Label("Lose Percentage :" + twoDigit.format(loseP));
        avgCr = new Label("Average credits :" + twoDigitSign.format(avgCredits));
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(5));
        stats.setSpacing(20);
        stats.getChildren().addAll(winPer, losePer,avgCr, btnSaveStat);

        root.getChildren().add(pieChart);
        root.getChildren().add(stats);
        stage.setScene(new Scene(root, 600, 350));

        stage.show();

    }

    //method which returns the data needed to the pie chart in stat window
    private static ObservableList<Data> getChart(double winP, double loseP) {

        ObservableList<Data> answer = FXCollections.observableArrayList();
        answer.addAll(new PieChart.Data("Win", winP),
                new PieChart.Data("Lose", loseP));
        return answer;
    }
    
    //save calculated stats
    public static String[] getSaveData(String winP, String loseP, String avg){
        String[] fileData=new String[4];
        fileData[0]="Win Percentage :"+winP+"%";
        fileData[1]="Lose Percentage :"+loseP+"%";
        fileData[2]="Average credits Won :"+avg;
        fileData[3]="Payout Percentage :"+fnlPayout+"%";
        return fileData;
    }
    
    
    /*this method will try to keep the payout pecentage at a given constant rate by increasing and decreasing the speed of the reel*/
    public static double payoutControl(double totSpent/*total spendings*/,double totEarn/*total winnings*/,double limit/*payout limit*/){
    
    	double balancer=4;
    	totEarn+=balancer;
    	totSpent+=balancer-1;
    	double payout = totEarn/totSpent;
    	fnlPayout=payout*100;
    	double deviation=payout-limit;//deviation from the payout
    	double increment=0;
    	if(deviation<=0){
    		increment = 100 * (deviation/limit);
    	}else{
    		if(deviation<=(limit*2)){
    			increment = 100 * (deviation/(limit*2));
    		}else{
    			increment=100;
    		}
    	}
    	
    	return -increment;
    }

    //method save stats to a file
    public static void saveToFile(String[] fileData) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd__HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();

        try (FileWriter fw = new FileWriter("__" + dtf.format(now) + ".txt")) {
            try (PrintWriter pw = new PrintWriter(fw, true)) {
                for(String dataLine:fileData){
                    pw.println(dataLine);
                }
            }

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    //inner class which is to create threads
    public class ReelThread implements Runnable {

        Symbol[] symArr;
        Label lbl;
        KeyFrame frameRate;
        Timeline animationReel;
        int imgIndex = 0;
        int imgIndexChoosen = 0;
        boolean spinning = false;
        EventHandler<ActionEvent> eventHandler;
       // double speed=120;

        public ReelThread(Symbol[] symArr, Label lbl) {
            this.symArr = symArr;
            this.lbl = lbl;
        }
        @Override
        public void run() {

        	//Platform.runLater() to improve the performance of the ui
            Platform.runLater(() -> {
            	//EventHandler which changes the image 
                eventHandler = e -> {
                    if (imgIndex < 6) {
                    	//making the reels auto-resizable
                        symArr[imgIndex].getImage().fitWidthProperty().bind(gamePane.widthProperty().divide(4));
                        symArr[imgIndex].getImage().fitHeightProperty().bind(gamePane.heightProperty().divide(1.2));
                        lbl.setGraphic(symArr[imgIndex].getImage());
                        
                        //Fx animations
                        FadeTransition ft = new FadeTransition(Duration.millis(750), symArr[imgIndex].getImage());
                        ft.setFromValue(0.5);
                        ft.setToValue(1);
                        ft.play();
                        imgIndex++;
                        if (imgIndex == 6) {
                            imgIndex = 0;
                        }

                    }

                };
                //Fx animations
                lbl.setOnMousePressed((MouseEvent me) -> {
                	//stooping the reel
                    animationReel.pause();
                    if (imgIndex == 0) {
                        imgIndexChoosen = 5;
                    } else {
                        imgIndexChoosen = imgIndex - 1;
                    }

                    spinning = false;
                    if (!(reel0.spinning || reel1.spinning || reel2.spinning || checkedWin)) {
                        checkWin();
                    }
                });
            });

        }

        //start spinning the reels
        private void startR() {
            spinning = true;
            frameRate=new KeyFrame(Duration.millis( 120+speedControl ), eventHandler);
            animationReel = new Timeline(frameRate);
            animationReel.setCycleCount(Timeline.INDEFINITE);
            animationReel.play();


        }

    }

    //exiting the program
    @Override
    public void stop() {
        System.exit(0);
    }
}
