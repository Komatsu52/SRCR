import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class parser{

    public static void main(String[] args) throws IOException {

        double raioTerra = 6372.795477598;

        String[][] front = {{"Porto","Viana do Castelo", "Vila Real"},
                {"Braga"},
                {"Braga", "Vila Real", "Aveiro", "Viseu"},
                {"Porto", "Braga", "Bragança", "Viseu"},
                {"Vila Real", "Guarda", "Viseu"},
                {"Viseu","Porto", "Coimbra"},
                {"Aveiro", "Porto", "Vila Real", "Guarda", "Bragança", "Coimbra"},
                {"Bragança", "Castelo Branco", "Viseu", "Coimbra"},
                {"Aveiro", "Viseu", "Leiria", "Castelo Branco", "Guarda"},
                {"Guarda", "Coimbra", "Leiria", "Santarém", "Portalegre"},
                {"Coimbra", "Castelo Branco", "Santarém", "Lisboa"},
                {"Leiria", "Lisboa", "Setúbal", "Portalegre", "Évora", "Castelo Branco"},
                {"Castelo Branco", "Santarém", "Évora"},
                {"Leiria", "Santarém"},
                {"Évora", "Beja", "Santarém"},
                {"Setúbal", "Santarém", "Portalegre", "Beja"},
                {"Setúbal", "Évora", "Faro"},
                {"Beja"}};

        String[] dist = {"Braga", "Viana do Castelo", "Porto", "Vila Real", "Bragança",
                "Aveiro", "Viseu", "Guarda", "Coimbra", "Castelo Branco", "Leiria",
                "Santarém", "Portalegre", "Lisboa", "Setúbal", "Évora", "Beja", "Faro"};

        Map<String, List<String>> fronteiras = new HashMap<>();
        Map<String, List<String>> concelhos = new HashMap<>();
        Map<String, double[]> coordenadas = new HashMap<>();
        List<String> aux;

        for(int i = 0; i < dist.length; i++){
            for(int j = 0; j < front[i].length; j++) {
                aux = fronteiras.getOrDefault(front[i][j], new ArrayList<>());
                if(!(aux.contains(dist[i]))){
                    aux = fronteiras.getOrDefault(dist[i], new ArrayList<>());
                    aux.add(front[i][j]);
                    fronteiras.put(dist[i], aux);
                }
            }

            concelhos.put(dist[i], new ArrayList<>());
        }

        int id = -1, i;
        String cidade = " ", distrito = " ", capital = " ";
        double lat = -1, lon = -1;

        FileWriter cidfile = new FileWriter("cidades.pl");
        FileWriter ligfile = new FileWriter("ligacoes.pl");

        File excelFile = new File("/home/goncalo/Área de Trabalho/SRCR/TrabalhoIndividual/cidades.xlsx");
        FileInputStream fis = new FileInputStream(excelFile);

        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);

        Iterator<Row> rowIt = sheet.iterator();
        Row row;

        if(rowIt.hasNext()){
            row = rowIt.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            i = 0;

            cidfile.write("%cidade(");

            while (cellIterator.hasNext() && i < 6) {
                Cell cell = cellIterator.next();

                if(i < 5)
                    cidfile.write(cell.toString().toUpperCase() + ", ");
                else
                    cidfile.write(cell.toString().toUpperCase() + ").\n\n");

                i++;
            }
        }

        while(rowIt.hasNext()) {
            row = rowIt.next();
            i = 0;

            Iterator<Cell> cellIterator = row.cellIterator();

            while (cellIterator.hasNext() && i < 6) {
                Cell cell = cellIterator.next();
                switch (i){
                    case 0:
                        id = (int) Double.parseDouble(cell.toString());
                        break;
                    case 1:
                        cidade = cell.toString();
                        break;
                    case 2:
                        lat = Double.parseDouble(cell.toString());
                        break;
                    case 3:
                        lon = Double.parseDouble(cell.toString());
                        break;
                    case 4:
                        distrito = cell.toString();
                        break;
                    case 5:
                        capital = cell.toString();
                        break;
                }

                i++;
            }

            aux = concelhos.get(distrito);
            aux.add(cidade);
            concelhos.put(distrito, aux);

            cidfile.write("cidade(" + id + ", \"" + cidade + "\", " + lat + ", " + lon + ", \"" + distrito + "\", " + capital + ").\n");
            double[] coord = new double[2];
            coord[0] = lat;
            coord[1] = lon;
            coordenadas.put(cidade, coord);
        }

        List<String> aux2, aux3, sortedDist = new ArrayList<>(concelhos.keySet());
        Collections.sort(sortedDist);
        double distancia;

        ligfile.write("%ligacao(CIDADE1, CIDADE2, DISTANCIA).\n\n");

        for(String d : sortedDist){
            aux = fronteiras.get(d);
            aux2 = concelhos.get(d);
            Collections.sort(aux2);
            aux3 = new ArrayList<>();

            for(String c : aux2){
                for(String c2 : aux3) {
                    if(!(c.equals(c2))) {
                        double[] coord1 = coordenadas.get(c);
                        double latA = (coord1[0]*3.14)/180;
                        double lonA = (coord1[1]*3.14)/180;
                        double[] coord2 = coordenadas.get(c2);
                        double latB = (coord2[0]*3.14)/180;
                        double lonB = (coord2[1]*3.14)/180;
                        distancia = raioTerra * Math.acos(Math.sin(latA)*Math.sin(latB) + Math.cos(latA)*Math.cos(latB)*Math.cos(lonA-lonB));
                        ligfile.write("ligacao(\"" + c + "\", \"" + c2 + "\", " + distancia + ").\n");
                    }
                }
                aux3.add(c);
            }

            if(aux != null){
                for(String f : aux){
                    aux3 = concelhos.get(f);
                    Collections.sort(aux3);

                    for(String c : aux2){
                        for(String c2 : aux3) {
                            double[] coord1 = coordenadas.get(c);
                            double latA = (coord1[0]*3.14)/180;
                            double lonA = (coord1[1]*3.14)/180;
                            double[] coord2 = coordenadas.get(c2);
                            double latB = (coord2[0]*3.14)/180;
                            double lonB = (coord2[1]*3.14)/180;
                            distancia = raioTerra * Math.acos(Math.sin(latA)*Math.sin(latB) + Math.cos(latA)*Math.cos(latB)*Math.cos(lonA-lonB));
                            ligfile.write("ligacao(\"" + c + "\", \"" + c2 + "\", " + distancia + ").\n");
                        }
                    }
                }
            }
        }

        workbook.close();
        fis.close();
        cidfile.close();
        ligfile.close();
    }
}
