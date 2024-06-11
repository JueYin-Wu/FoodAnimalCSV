import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LectorCSV {

    static List<AnimalFoodOptimization.FoodItem> foodItems = new ArrayList<>();
    String grupo;
    String especie;
    String variedad;
    String nombre;
    String unidadPrecio;
    double precioMin;
    double precioMax;
    String categoria;
    double calidad;


    public void lectorApache() {

        String filePath = "C:\\Users\\wumaf\\OneDrive - Universidad de Montevideo\\UM PC\\Operations Research\\Final Project\\Fork\\UAM_Precios.csv";

        try (FileReader reader = new FileReader(filePath);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {
            int nroFila = 1;
            for (CSVRecord csvRecord : csvParser) {
                if (nroFila <= 2) {
                    nroFila += 1;
                } else {
                    try {
                        grupo = csvRecord.get(0);
                        especie = csvRecord.get(1);
                        variedad = csvRecord.get(2);
                        unidadPrecio = csvRecord.get(3);
                        precioMin = Float.parseFloat(csvRecord.get(4));
                        precioMax = Float.parseFloat(csvRecord.get(5));
                        categoria = csvRecord.get(6);

                        nombre = especie + " " + variedad;

                        if (precioMax == 0) {
                            precioMin = 9999;
                            precioMax = 9999;
                        }

                        switch (categoria) {
                            case "II" -> calidad = 0.6;
                            case "I" -> calidad = 0.8;
                            case "E" -> calidad = 0.95;
                        }

                        AnimalFoodOptimization.FoodItem alimento = new AnimalFoodOptimization.FoodItem(nombre, precioMin, precioMax, calidad);
                        foodItems.add(alimento);
                        System.out.println(alimento.getName() + " " + alimento.getMinPrice() + " " + alimento.getMaxPrice() + " " + alimento.getQuality());


                    } catch (NumberFormatException e) {

                    }

                }
            }
            //System.out.println("done! " + tweetsHash.getNroElementos() + " " + pilotosHash.getNroElementos());

        } catch (
        IOException e) {
            e.printStackTrace();
        }

    }
}

