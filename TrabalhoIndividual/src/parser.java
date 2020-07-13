import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Normalizer;
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


        String[][] mon = {{"Sintra", "Palácio da Pena", "Palácio de Queluz", "Palácio de Monserrate", "Castelo dos Mouros", "Quinta da Regaleira", "Palácio Nacional de Sintra"},
                          {"Porto", "Palácio da Bolsa", "Estação de São Bento", "Torre dos Clérigos"},
                          {"Monção", "Palácio da Brejoeira"},
                          {"Faro", "Palácio de Estói"},
                          {"Mafra", "Palácio Nacional de Mafra"},
                          {"Lisboa", "Palácio Nacional de Ajuda", "Mosteiro dos Jerónimos", "Torre de Belém", "Castelo de São Jorge", "Convento do Carmo"},
                          {"Vila Nova de Gaia", "Mosteiro da Serra do Pilar"},
                          {"Batalha", "Mosteiro da Batalha"},
                          {"Alcobaça", "Mosteiro de Alcobaça"},
                          {"Braga", "Mosteiro de Tibães", "Bom Jesus do Monte"},
                          {"Évora", "Cromeleque dos Almendres", "Capela dos Ossos", "Templo Romano de Évora"},
                          {"Guimarães", "Castelo de Guimarães"},
                          {"Chaves", "Ponte Romana de Trajano"},
                          {"Vila Real", "Palácio de Mateus"},
                          {"Lamego", "Santuário de Nossa Senhora dos Remédios"},
                          {"Viana do Castelo", "Santa Luzia"},
                          {"Mealhada", "Palácio do Buçaco"},
                          {"Melgaço", "Nossa Senhora da Peneda"},
                          {"Tomar", "Convento de Cristo"},
                          {"Guarda", "Sé da Guarda"},
                          {"Ovar", "Igreja Paroquial de Válega"},
                          {"Coimbra", "Biblioteca Joanina de Coimbra"}};

        Map<String, List<String>> fronteiras = new HashMap<>();
        Map<String, List<String>> concelhos = new HashMap<>();
        Map<String, double[]> coordenadas = new HashMap<>();
        Map<String, List<String>> monumentos = new HashMap<>();
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

        for(int i = 0; i < mon.length; i++){
            aux = new ArrayList<>();
            String cidade = mon[i][0];
            for(int j = 1; j < mon[i].length; j++)
                aux.add(mon[i][j]);
            monumentos.put(cidade, aux);
        }

        int id = -1, i;
        String cidade = " ", distrito = " ", capital = " ";
        double lat = -1, lon = -1;

        FileWriter cidfile = new FileWriter("cidades.pl");
        FileWriter ligfile = new FileWriter("ligacoes.pl");
        FileWriter monfile = new FileWriter("monumentos.pl");
        FileWriter turfile = new FileWriter("turismo.pl");

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
                    cidfile.write(cell.toString().toUpperCase() + ").\n");

                i++;
            }

            monfile.write("%monumento(MONUMENTO, CIDADE).\n");
            turfile.write("%turismo(TIPO, CIDADE).\n");
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

            cidfile.write(Normalizer.normalize("\ncidade(" + id + ", '" + cidade + "', " + lat + ", " + lon + ", '" + distrito + "', " + capital + ").", Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
            double[] coord = new double[2];
            coord[0] = lat;
            coord[1] = lon;
            coordenadas.put(cidade, coord);

            if(monumentos.containsKey(cidade)){
                for(String m : monumentos.get(cidade))
                    monfile.write(Normalizer.normalize("\nmonumento('" + m + "', '" + cidade + "').", Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
            }

            Random rand = new Random();
            int r = rand.nextInt(6);

            switch (r){
                case 0:
                    turfile.write(Normalizer.normalize("\nturismo(gastronomico, '" + cidade + "').", Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                    break;
                case 1:
                    turfile.write(Normalizer.normalize("\nturismo(cultural, '" + cidade + "').", Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                    break;
                case 2:
                    turfile.write(Normalizer.normalize("\nturismo(balnear, '" + cidade + "').", Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                    break;
                default:
                    break;
            }
        }

        List<String> aux2, aux3, sortedDist = new ArrayList<>(concelhos.keySet());
        Collections.sort(sortedDist);
        double distancia;

        ligfile.write("%ligacao(CIDADE1, CIDADE2, DISTANCIA).\n");

        for(String d : sortedDist){
            aux = fronteiras.get(d);
            aux2 = concelhos.get(d);
            Collections.sort(aux2);
            aux3 = new ArrayList<>();

            for(String c : aux2){
                for(String c2 : aux3) {
                    if(!(c.equals(c2))) {
                        double[] coord1 = coordenadas.get(c);
                        double latA = (coord1[0]*Math.PI)/180;
                        double lonA = (coord1[1]*Math.PI)/180;
                        double[] coord2 = coordenadas.get(c2);
                        double latB = (coord2[0]*Math.PI)/180;
                        double lonB = (coord2[1]*Math.PI)/180;
                        distancia = raioTerra * Math.acos(Math.sin(latA)*Math.sin(latB) + Math.cos(latA)*Math.cos(latB)*Math.cos(lonA-lonB));
                        ligfile.write(Normalizer.normalize("\nligacao('" + c + "', '" + c2 + "', " + distancia + ").", Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
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
                            double latA = (coord1[0]*Math.PI)/180;
                            double lonA = (coord1[1]*Math.PI)/180;
                            double[] coord2 = coordenadas.get(c2);
                            double latB = (coord2[0]*Math.PI)/180;
                            double lonB = (coord2[1]*Math.PI)/180;
                            distancia = raioTerra * Math.acos(Math.sin(latA)*Math.sin(latB) + Math.cos(latA)*Math.cos(latB)*Math.cos(lonA-lonB));
                            ligfile.write(Normalizer.normalize("\nligacao('" + c + "', '" + c2 + "', " + distancia + ").", Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                        }
                    }
                }
            }
        }

        workbook.close();
        fis.close();
        cidfile.close();
        ligfile.close();
        monfile.close();
        turfile.close();
    }
}
