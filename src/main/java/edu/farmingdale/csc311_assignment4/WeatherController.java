package edu.farmingdale.csc311_assignment4;
/**
 * @author Ahnaf Sindid
 */

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;


public class WeatherController {
    /**
     * TextFields
     */

    @FXML
    private TextField DateField;

    @FXML
    private TextField OutputField;

    @FXML
    private TextField ThresholdField;


    private List<weatherData> weatherdata = new ArrayList<>();

    /**
     *weather data record
     */
    record weatherData(LocalDate Date, double Temperature, int Humidity, double Precipitation){}

    /**
     * initialize method
     */

    @FXML
    private void initialize() {
        try{
            weatherdata = parseWeatherData("/weatherdata.csv");
        }catch (IOException e){
            OutputField.setText("Error");
        }
    }

    /**
     * parsing the csv data
     * @param resourcePath path to the resource
     * @return weather records
     * @throws IOException if there is no file found or read
     */
    private List<weatherData> parseWeatherData(String resourcePath) throws IOException {
        List<weatherData> data = new ArrayList<>();
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))){
            String line;
            br.readLine();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] val = line.split(",");
                if(val.length < 4) continue;
                try{
                    LocalDate Date = LocalDate.parse(val[0].trim(), formatter);
                    double Temperature = Double.parseDouble(val[1].trim());
                    int Humidity = Integer.parseInt(val[2].trim());
                    double Precipitation = Double.parseDouble(val[3].trim());
                    data.add(new weatherData(Date, Temperature, Humidity, Precipitation));
                }catch (Exception e){
                    System.out.println("Skipping invalid" + line);
                }
            }
        }
        return data;
    }

    /**
     * Average temperature button for each month from the csv
     * @param event happens when you click the button
     */

    @FXML
    void AvgTemperatureButton(ActionEvent event) {
        try{
            int month = Integer.parseInt(DateField.getText().split("/")[0]);
            if(month < 1 || month > 12) throw new IllegalArgumentException("Error");
            double avg = weatherdata.stream()
                    .filter(weatherData -> weatherData.Date().getMonthValue() == month)
                    .mapToDouble(weatherData::Temperature)
                    .average()
                    .orElse(Double.NaN);
            Platform.runLater(() -> OutputField.setText(String.format("%.2f", avg)));
        }catch (Exception e){
            Platform.runLater(() -> OutputField.setText("Error"));
        }

    }

    /**
     * days for threshold button
     * @param event happens when you click the button
     */

    @FXML
    void DaysThresholdButton(ActionEvent event) {
        try{
            double threshold = Double.parseDouble(ThresholdField.getText());
            int month = Integer.parseInt(DateField.getText().split("/")[0]);
            long count = weatherdata.stream()
                    .filter(weatherData -> weatherData.Date().getMonthValue() == month && weatherData.Temperature() > threshold)
                    .count();
            Platform.runLater(() -> OutputField.setText(String.format("%d", count)));
        }catch(NumberFormatException e){
            Platform.runLater(() -> OutputField.setText("0"));
        }
    }

    /**
     * counting rainy days button
     * @param event happens when you click the button
     */

    @FXML
    void RainyCountButton(ActionEvent event) {
        try {
            int month = Integer.parseInt(DateField.getText().split("/")[0]);
            if(month < 1 || month > 12) throw new IllegalArgumentException("Error");
            long count = weatherdata.stream()
                    .filter(weatherData -> weatherData.Date().getMonthValue() == month && weatherData.Precipitation() > 0)
                    .count();
            Platform.runLater(() -> OutputField.setText(String.valueOf(count)));
        }catch(NumberFormatException e){
            Platform.runLater(() -> OutputField.setText("0"));
        }
    }

    /**
     * prints the weather category based on threshold
     * @param event happens when you click the button
     */

    @FXML
    void weatherCatBtn(ActionEvent event) {
        try{
            double temperature = Double.parseDouble(ThresholdField.getText());
            String category = switch ((int) temperature / 10){
                case 0, 1, 2, 3 -> "Cold";
                case 4,5,6 -> "Warm";
                case 7,8,9, 10-> "Hot";
                default -> "Unkown";
            };
            OutputField.setText(category);
        }catch (NumberFormatException e){
            OutputField.setText("Error");
        }

    }

}


