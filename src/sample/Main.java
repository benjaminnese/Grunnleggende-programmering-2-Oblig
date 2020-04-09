package sample;

import java.sql.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import static java.lang.Integer.parseInt;

//OBLIG_PROG av Benjamin Nese
public class Main extends Application {
    //objekt variabel for å bruke seFullført på to måter
    int teller = 0;

    public void start(Stage vindu) throws Exception {

        //Oppretter kobling til database, oppretter oppgaver.db  og database om den ikke eksiterer
        Connection con = DriverManager.getConnection("jdbc:sqlite:oppgaver.db");
        String querry = "CREATE TABLE IF NOT EXISTS oppgaver" +
                " ( id INTEGER PRIMARY KEY NOT NULL," +
                "oppgave TEXT NOT NULL," +
                "fullført INTEGER NOT NULL); ";
        Statement stmt = con.createStatement();
        stmt.execute(querry);

        //Sett op vindupanel med forskjellige soner, top, bunn, center osv
        BorderPane panel = new BorderPane();

        //Lager menyrad for knapper
        HBox menyrad = new HBox();
        menyrad.setPadding(new Insets(10));
        //Div HBox og VBox for innhold
        HBox innhold = new HBox();
        VBox leggTil = new VBox();
        HBox erFullført = new HBox();

        //Text bokser for tekts innhold
        TextArea textBox = new TextArea();
        TextField nyOppgave = new TextField();
        TextField settFullførtTxt = new TextField();


        //Knapper for å se oppgaver og legge til nye oppgaver
        Button btnAapneOppgaver = new Button("Hvis alle oppgaver");
        Button btnHvisBareFullført = new Button("Fullført/Ikke Fullført");
        Button btnLeggTilOppgaver = new Button("Legg til oppgave");
        Button btnLeggTil = new Button("Legg til");
        Button btnSettFullført = new Button("Oppgave fullført (ID)");

        //Knapp for å viser alle oppgaver i databasen
        btnAapneOppgaver.setOnAction(event -> {
            clearDatShit(innhold, erFullført, leggTil);


            try {
                textBox.setText(seOppgaver());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            innhold.getChildren().add(textBox);

        });

        btnHvisBareFullført.setOnAction(event -> {
            clearDatShit(innhold, erFullført, leggTil);
            teller++;
            try {
                textBox.setText(seFullførtOppgaver(teller));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            innhold.getChildren().add(textBox);
            erFullført.getChildren().addAll(settFullførtTxt, btnSettFullført);
            panel.setBottom(erFullført);

        });

        //knapp for å legge til nye oppgaver
        btnLeggTilOppgaver.setOnAction(event -> {
            clearDatShit(innhold, erFullført, leggTil);
            leggTil.getChildren().addAll(nyOppgave, btnLeggTil);
            innhold.getChildren().add(leggTil);
        });

        //knapp for å sumbite nye oppgaven
        btnLeggTil.setOnAction(event -> {

            try {
                leggTil(nyOppgave.getText());
                nyOppgave.setText("");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        //Setter enn oppgave som fullført
        btnSettFullført.setOnAction(event -> {
            try {
                settFullfort(parseInt(settFullførtTxt.getText()));
                settFullførtTxt.setText("");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //Når bruker endrer oppgave til fullført, viser JavaFX alle fullførte oppgaver
            try {
                textBox.setText(seFullførtOppgaver(1));
                clearDatShit(innhold, erFullført, leggTil);
                innhold.getChildren().add(textBox);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });

        //"arver" knappene til HBox så de ligger ved sidenav hverandre
        menyrad.getChildren().addAll(btnAapneOppgaver, btnHvisBareFullført, btnLeggTilOppgaver);

        //Sett menyraden i sonen top i BorderPane vindupanelet
        panel.setTop(menyrad);
        panel.setCenter(innhold);

        Scene scene = new Scene(panel, 450, 500);    //

        vindu.setScene(scene);
        vindu.setTitle("Gjøremål");
        vindu.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

//-----------------------------------------------------------Metoder-----------------------------------------------//


    //Se oppgaver i databasen
    public static String seOppgaver() throws SQLException {

        Connection con = DriverManager.getConnection("jdbc:sqlite:oppgaver.db");

        String queery = "SELECT * FROM oppgaver ORDER BY id";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(queery);

        String oppgave = "";
        //Looper gjennom databasen
        while (rs.next()) {
            //Henter neste oppgave i databasen, setter den i en String
            oppgave += "ID " + rs.getInt("ID") +
                    " \nOppgave: " + rs.getString("oppgave") + "\n\n";
        }
        con.close();
        stmt.close();
        return oppgave;
    }

    public static String seFullførtOppgaver(int teller) throws SQLException {

        Connection con = DriverManager.getConnection("jdbc:sqlite:oppgaver.db");
        String queery;
        String fullførteOppgaver;

        if (teller % 2 == 1) {
            fullførteOppgaver = "OPPGAVE FULLFØRT\n\n";
        } else {
            fullførteOppgaver = "OPPGAVE IKKE FULLFØRT\n\n";
        }
        queery = "SELECT * FROM oppgaver WHERE fullført = " + (teller%2) + ";";

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(queery);

        while (rs.next()) {
            fullførteOppgaver += "ID: " + rs.getInt("ID") + " " +
                    rs.getString("Oppgave") + " \n";
        }
        con.close();
        stmt.close();
        return fullførteOppgaver;
    }

    ;

    //legg til nye oppgaver
    public static void leggTil(String oppgaveInput) throws SQLException {

        String queery = "INSERT INTO oppgaver(oppgave,fullført) " +
                "VALUES(?,?)";

        Connection con = DriverManager.getConnection("jdbc:sqlite:oppgaver.db");
        PreparedStatement pstmt = pstmt = con.prepareStatement(queery);

        pstmt.setString(1, oppgaveInput);
        pstmt.setInt(2, 0);
        pstmt.executeUpdate();
        pstmt.close();
        con.close();
    }

    //Sett oppgave som fullført
    public static void settFullfort(int idInput) throws SQLException {

        String queery = "UPDATE oppgaver SET fullført = ? where ID = " + idInput + ";";

        Connection con = DriverManager.getConnection("jdbc:sqlite:oppgaver.db");
        PreparedStatement pstmt = pstmt = con.prepareStatement(queery);

        pstmt.setInt(1, 1);
        pstmt.executeUpdate();
        pstmt.close();
        con.close();

    }
    //Renser innholdet i boksene
    public static void clearDatShit(HBox innhold, HBox erFullført, VBox leggTil) {
        innhold.getChildren().clear();
        erFullført.getChildren().clear();
        leggTil.getChildren().clear();
    }
}