package dev.langchain4j.service.fastreflection;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.tool.ByteBuddyToolExecutor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import net.bytebuddy.ByteBuddy;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LambdaMetafactoryBeanPropertyReaderTest {



    @Test
    public void test() {

        PersonTool tool = new PersonTool();

        LambdaMetafactoryBeanPropertyReader reader = new LambdaMetafactoryBeanPropertyReader(
            PersonTool.class
        );

        reader.executeMethod(tool, "save");
    }

    @Test
    void test2() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        PersonTool tool = new ByteBuddy()
            .subclass(PersonTool.class)
            .make()
            .load(getClass().getClassLoader())
            .getLoaded().getConstructor().newInstance();

        tool.save();

    }


    public static class Person {

        String name;
        int age;
    }


    public static class PersonTool {

        @Tool
        public Person save() {
            return null;
        }

        @Tool
        public Person[] saveArray(Person[] arg) {
            assert arg != null;
            assert arg.length == 2;
            assert arg[0].getClass() == Person.class;
            assert arg[0].name.equals("Klaus");
            assert arg[0].age == 42;
            assert arg[1].getClass() == Person.class;
            assert arg[1].name.equals("Peter");
            assert arg[1].age == 43;
            return arg;
        }

        @Tool
        public List<Person> saveList(List<Person> personList) {
            assert personList != null;
            assert personList.size() == 2;
            personList.forEach(person -> {
                assert person.getClass() == Person.class;
            });
            assert personList.get(0).name.equals("Klaus");
            assert personList.get(0).age == 42;
            assert personList.get(1).name.equals("Peter");
            assert personList.get(1).age == 43;
            return personList;
        }

        @Tool
        public Set<Person> saveSet(Set<Person> personSet) {
            assert personSet != null;
            assert personSet.size() == 2;
            personSet.forEach(person -> {
                assert person.getClass() == Person.class;
            });
            assert personSet.stream().anyMatch(person -> person.name.equals("Klaus") && person.age == 42);
            assert personSet.stream().anyMatch(person -> person.name.equals("Peter") && person.age == 43);
            return personSet;
        }

        @Tool
        public Map<String, Person> saveMap(Map<String, Person> idPersonMap) {
            assert idPersonMap != null;
            assert idPersonMap.size() == 2;
            idPersonMap.forEach((id, person) -> {
                assert id.getClass() == String.class;
                assert person.getClass() == Person.class;
            });
            assert idPersonMap.get("p1").name.equals("Klaus");
            assert idPersonMap.get("p1").age == 42;
            assert idPersonMap.get("p2").name.equals("Peter");
            assert idPersonMap.get("p2").age == 43;
            return idPersonMap;
        }
    }


}
