import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Copyright 2018 kavoc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

public class Race {
    private int ID;
    private IChannel channel;

    private String game;
    private String mode;
    private boolean casual;

    private HashMap<String, Racer> racers = new HashMap<String, Racer>();
    private State state = State.Prerace;
    private long startTime = 0;
    private Timer f;

    public enum State {Prerace, Counting_Down, In_Progress, Finished, Finalized}

    private Racebot racebot;

    public void checkForRaceFinish(){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Racebot.log(e.getMessage());
        }

        boolean undone = false;
        for (String r : racers.keySet()){
            Racer racer = racers.get(r);
            if (racer.getState() != Racer.State.Finished && racer.getState() != Racer.State.Forfeit) undone = true;
        }

        if (undone == false){
            state = State.Finished;

            Racebot.requestedMessage(channel, "Race "+getID()+" has finished.  GG to all the racers!");

            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    if (state == State.Finished) {
                        try {
                            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                            DateFormat tod = new SimpleDateFormat("HH:mm");
                            Date date = new Date();
                            int casualInt = 0;
                            if (casual) casualInt = 1;

                            String sql = "INSERT into races (number, date, time, game, mode, casual) values (?, ?, ?, ?, ?, ?)";
                            PreparedStatement racesPS = Racebot.database.prepareStatement(sql);
                            racesPS.setInt(1, getID());
                            racesPS.setString(2, df.format(date));
                            racesPS.setString(3, tod.format(date));
                            racesPS.setString(4, game);
                            racesPS.setString(5, mode);
                            racesPS.setBoolean(6, casual);
                            racesPS.execute();


                            sql = "create table race" + getID() + "(name varchar(255) PRIMARY KEY, time INTEGER)";
                            Statement statement = Racebot.database.createStatement();
                            statement.execute(sql);

                            String dbGameMode =  sqlName(game + " - " + mode);

                            sql = "INSERT into race" + getID() + "(name, time) values (?,?)";
                            PreparedStatement racePS = Racebot.database.prepareStatement(sql);
                            sql = "SELECT * FROM runner_lookup WHERE name = ?";
                            PreparedStatement runnerTablePS = Racebot.database.prepareStatement(sql);
                            sql = "insert or replace into runner_lookup (runner_key, name) values (?,?)";
                            PreparedStatement newRunnerLookupPS = Racebot.database.prepareStatement(sql);

                            for (String r : racers.keySet()) {
                                //Record race results
                                Racer racer = racers.get(r);
                                racePS.setString(1, r);
                                racePS.setInt(2, (int)racer.time);
                                racePS.execute();

                                //Find user in lookup table
                                String runnerTable = "";
                                runnerTablePS.setString(1,racer.name+racer.discriminator);
                                ResultSet runnerTableResults = runnerTablePS.executeQuery();
                                while (runnerTableResults.next()){
                                    runnerTable = runnerTableResults.getString("runner_key");
                                }

                                //User was not found, add to runner_translations and create the new table tracking this runner
                                if (runnerTable.equals("")) {
                                    //Lookup last used key to determine next available key
                                    sql = "SELECT * FROM runner_lookup ORDER BY runner_key DESC LIMIT 1";
                                    ResultSet lastIndexResult = statement.executeQuery(sql);

                                    long nextAvailableKey = 0;
                                    while (lastIndexResult.next()) {
                                        String lastUserKey = lastIndexResult.getString("runner_key");
                                        nextAvailableKey = Long.parseLong(lastUserKey.substring(6))+1;
                                    }

                                    //Add new key to runner_lookup
                                    String newRunnerTableName = "runner"+nextAvailableKey;
                                    newRunnerLookupPS.setString(1, newRunnerTableName);
                                    newRunnerLookupPS.setString(2, racer.name+racer.discriminator);
                                    newRunnerLookupPS.execute();

                                    //Create new table for runner
                                    sql = "create table if not exists ? (gamemode varchar(255) PRIMARY KEY, best_time INTEGER NOT NULL, score INTEGER NOT NULL)";
                                    PreparedStatement statement = Racebot.database.prepareStatement(sql);
                                    statement.setString(1, newRunnerTableName);
                                    statement.execute();


                                    runnerTable = newRunnerTableName;
                                }

                                //Check if the runner's best time was beaten and pull their current rating
                                sql = "select * from ? where gamemode=?";
                                PreparedStatement statement = Racebot.database.prepareStatement(sql);
                                statement.setString(1, runnerTable);
                                statement.setString(2, dbGameMode);
                                ResultSet runnersTableResults = statement.executeQuery();

                                boolean result = false;
                                while (runnersTableResults.next()){
                                    result = true;
                                    long bestTime = runnersTableResults.getLong("best_time");
                                    racer.previousRating = runnersTableResults.getInt("score");

                                    if (racer.time < bestTime){
                                        sql = "update " + runnerTable + " set best_time=" + racer.time + " where gamemode='" + dbGameMode + "'";
                                        statement.execute(sql);
                                    }
                                }

                                //A current best was not found, so use this one
                                if (!result){
                                    sql = "insert into " + runnerTable + "(gamemode, best_time, score) values('" + dbGameMode + "', " + racer.time + ", 1500)";
                                    statement.execute(sql);
                                }
                            }

                            //This race is for points
                            if (!casual){
                                for (String r : racers.keySet()){
                                    Racer r1 = racers.get(r);
                                    double changeRating = 0;
                                    for (String s : racers.keySet()){
                                        Racer r2 = racers.get(s);

                                        double expected = 1d / (1d + Math.pow(10d, ((r2.previousRating - r1.previousRating) / 400d)));
                                        if (r1.time < r2.time) {
                                            changeRating += (1d - expected) * 32d;
                                        } else if (r1.time > r2.time){
                                            changeRating += (0d - expected) * 32d;
                                        } else {
                                            changeRating += 0;
                                        }
                                    }

                                    String r1Table = "";
                                    runnerTablePS.setString(1,r1.name+r1.discriminator);
                                    ResultSet runnerTableResults = runnerTablePS.executeQuery();
                                    while (runnerTableResults.next()){
                                        r1Table = runnerTableResults.getString("runner_key");
                                    }

                                    //TODO PreparedStatement would be better here
                                    long score = r1.previousRating + Math.round(changeRating);
                                    sql = "update " + r1Table + " set score= " + score + " where gamemode='" + dbGameMode + "'";
                                    statement.execute(sql);

                                }
                            }

                        } catch (SQLException e) {
                            e.printStackTrace();
                            Racebot.log(e.getMessage());
                            System.out.println(e.getMessage());
                        }
        }
    }
}
