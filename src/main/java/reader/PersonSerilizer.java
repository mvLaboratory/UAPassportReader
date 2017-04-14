package reader;

import Models.Person;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class PersonSerilizer {
    public static void SavePerson(String path, Person person) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writeValue(new File(path + "\\person.json"), person);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
