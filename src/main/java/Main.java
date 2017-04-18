import Models.Person;
import org.jmrtd.BACKey;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.*;
import net.sf.scuba.smartcards.CardFileInputStream;
import reader.PersonSerilizer;
import reader.TerminalCardService;
import javax.imageio.ImageIO;
import javax.smartcardio.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) throws IOException {
        Logger globalLogger = Logger.getLogger("");
        globalLogger.setUseParentHandlers(false);
        globalLogger.setLevel(Level.OFF);

        if (args.length < 4) {
            System.out.println("Not enough arguments!");
            return;
        }

        String passportNumber = args[0];
        String birthDate = args[1];
        String expirationDate = args[2];
        String fileTemp = args[3];
        fileTemp = fileTemp.replaceAll("\\?", " ");

        System.setOut(new PrintStream("" + fileTemp + "\\log.txt"));

        BACKeySpec bacKey = new BACKey(passportNumber, birthDate, expirationDate);

        TerminalFactory factory = TerminalFactory.getDefault();
        CardTerminals terminals = factory.terminals();

        CardTerminal cardTerminal;
        PassportService passportService;

        try {
            cardTerminal = terminals.list().get(0);

            TerminalCardService cardService = new TerminalCardService(cardTerminal);

            cardService.open();

            passportService = new PassportService(cardService);
            passportService.open();
        } catch (Exception e) {
            if ((args.length >= 5) && (args[4].equals("-test"))) {
                testOutput(fileTemp);
            }
            else {
                System.out.println("Card is missing!");
                e.printStackTrace();
            }
            return;
        }

        byte[] aid = {1, 1};

        try {
            passportService.sendSelectApplet(aid);
            passportService.doBAC(bacKey);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Wrong security data!");
            return;
        }

        LDS lds = new LDS();
        DG1File dg1File;
        DG2File dg2File;
        DG7File dg7File;
        DG11File dg11File;
        DG12File dg12File;

        try {
            CardFileInputStream dg1In = passportService.getInputStream(PassportService.EF_DG1);
            lds.add(PassportService.EF_DG1, dg1In, dg1In.getLength());
            dg1File = lds.getDG1File();

            CardFileInputStream dg11In = passportService.getInputStream(PassportService.EF_DG11);
            lds.add(PassportService.EF_DG11, dg11In, dg11In.getLength());
            dg11File = lds.getDG11File();

            CardFileInputStream dg2In = passportService.getInputStream(PassportService.EF_DG2);
            lds.add(PassportService.EF_DG2, dg2In, dg2In.getLength());
            dg2File = lds.getDG2File();

            CardFileInputStream dg12In = passportService.getInputStream(PassportService.EF_DG12);
            lds.add(PassportService.EF_DG12, dg12In, dg12In.getLength());
            dg12File = lds.getDG12File();


            List<FaceImageInfo> allFaceImageInfos = new ArrayList<FaceImageInfo>();
            List<FaceInfo> faceInfos = dg2File.getFaceInfos();
            for (FaceInfo faceInfo : faceInfos) {
                allFaceImageInfos.addAll(faceInfo.getFaceImageInfos());
            }

            if (!allFaceImageInfos.isEmpty()) {
                FaceImageInfo faceImageInfo = allFaceImageInfos.iterator().next();
                int imageLength = faceImageInfo.getImageLength();

                DataInputStream dataInputStream = new DataInputStream(faceImageInfo.getImageInputStream());
                byte[] buffer = new byte[imageLength];
                dataInputStream.readFully(buffer, 0, imageLength);
                FileOutputStream fileOut2 = new FileOutputStream(fileTemp + "temp.jp2");
                fileOut2.write(buffer);
                fileOut2.flush();
                fileOut2.close();
                dataInputStream.close();

                File tempFile = new File(fileTemp + "temp.jp2");
                BufferedImage nImage = ImageIO.read(tempFile);
                if(tempFile.exists()) {
                    tempFile.delete();
                }
                ImageIO.write(nImage, "jpg", new File(fileTemp + "facePhoto.jpg"));
            }


            CardFileInputStream dg7In = passportService.getInputStream(PassportService.EF_DG7);
            lds.add(PassportService.EF_DG7, dg7In, dg7In.getLength());
            dg7File = lds.getDG7File();

            List<DisplayedImageInfo> signatureInfos = dg7File.getImages();
            for (DisplayedImageInfo signatureInfo : signatureInfos) {
                int imageLength = signatureInfo.getImageLength();

                DataInputStream dataInputStream = new DataInputStream(signatureInfo.getImageInputStream());
                byte[] buffer = new byte[imageLength];
                dataInputStream.readFully(buffer, 0, imageLength);
                FileOutputStream fileOut2 = new FileOutputStream(fileTemp + "tempSign.jp2");
                fileOut2.write(buffer);
                fileOut2.flush();
                fileOut2.close();
                dataInputStream.close();

                File tempFile = new File(fileTemp + "tempSign.jp2");
                BufferedImage nImage = ImageIO.read(tempFile);
                if(tempFile.exists()) {
                    tempFile.delete();
                }
                ImageIO.write(nImage, "jpg", new File(fileTemp + "signature.jpg"));
            }
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Error while reading passport data!");
            return;
        }


        MRZInfo mrzInfo = dg1File.getMRZInfo();
        Person person = new Person(dg11File.getNameOfHolder(), dg11File.getOtherNames(), dg11File.getFullDateOfBirth(), dg11File.getPlaceOfBirth(), mrzInfo.getGender().toString(), mrzInfo.getNationality(), mrzInfo.getDocumentNumber(), mrzInfo.getDateOfExpiry(), dg12File.getIssuingAuthority(), dg12File.getDateOfIssue());
        PersonSerilizer.SavePerson(fileTemp, person);

    }

    private static void testOutput(String path) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
        Date localBirthDate;
        Date docDateOfIssue;
        try {
            localBirthDate = dateFormatter.parse("01.01.2000");
            docDateOfIssue = dateFormatter.parse("01.01.2016");
        }
        catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        List<String> birthPlaces = new ArrayList<String>(){{
            add("Тернопіль");
            add("Тернопільський");
            add("Тернопільська");
            add("Україна");
        }};
        List<String> otherNames = new ArrayList<String>(){{
            add("Тестович");
        }};
        Person person = new Person("Тестенко<Тест<<Testenko<Test", otherNames, localBirthDate, birthPlaces, "Male", "ukr", "000111111", "010127", "0000", docDateOfIssue);
        PersonSerilizer.SavePerson(path, person);
    }
}
