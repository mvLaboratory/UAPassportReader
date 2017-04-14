package Models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Person implements Serializable {
    private String firstName = "";
    private String lastName = "";
    private String ukFirstName = "";
    private String ukLastName = "";
    private String fathersName = "";
    private Date dateOfBirth;
    private String placeOfBirth = "";
    private String gender = "";
    private String nationality = "";
    private String docNumber = "";
    private String docDateOExpiry = "";

    public Person() {
    }

    public Person(String names, List<String> otherNames, Date dateOfBirth, List<String> placeOfBirthList, String gender, String nationality, String docNumber, String docDateOExpiry) {
        parseName(names);

        if (otherNames.size() > 0) {
            fathersName = otherNames.get(0);
        }
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.nationality = nationality;
        this.docNumber = docNumber;
        this.docDateOExpiry = docDateOExpiry;
        setPlaceOfBirth(placeOfBirthList);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUkFirstName() {
        return ukFirstName;
    }

    public void setUkFirstName(String ukFirstName) {
        this.ukFirstName = ukFirstName;
    }

    public String getUkLastName() {
        return ukLastName;
    }

    public void setUkLastName(String ukLastName) {
        this.ukLastName = ukLastName;
    }

    public String getFathersName() {
        return fathersName;
    }

    public void setFathersName(String fathersName) {
        this.fathersName = fathersName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public String getDocDateOExpiry() {
        return docDateOExpiry;
    }

    public void setDocDateOExpiry(String docDateOExpiry) {
        this.docDateOExpiry = docDateOExpiry;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(List<String> placeOfBirthList) {
        StringBuilder  placeBuilder = new StringBuilder();
        for (String placePart : placeOfBirthList) {
            placeBuilder.append(placePart);
            placeBuilder.append(" ");
        }
        this.placeOfBirth = placeBuilder.toString();
    }

    private void parseName(String name) {
        String[] namesArr = name.split("<");
        ukFirstName = getNameByPos(namesArr, 1);
        ukLastName = getNameByPos(namesArr, 0);
        firstName = getNameByPos(namesArr, 4);
        lastName = getNameByPos(namesArr, 3);
    }

    private String getNameByPos(String[] namesArr, int pos) {
        return namesArr.length > pos ? namesArr[pos].replaceAll("<", "") : "";
    }

    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", ukFirstName='" + ukFirstName + '\'' +
                ", ukLastName='" + ukLastName + '\'' +
                ", fathersName='" + fathersName + '\'' +
                '}';
    }
}
