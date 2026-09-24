package pl.edu.pjwstk.lpfap.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.edu.pjwstk.lpfap.model.*;
import pl.edu.pjwstk.lpfap.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(ShelterRepository shelterRepo,
                           UserRepository userRepo,
                           AnimalRepository animalRepo,
                           ApplicationRepository appRepo,
                           AnalyticsReportRepository reportRepo,
                           PasswordEncoder passwordEncoder,
                           JdbcTemplate jdbc) {
        return args -> {

            reportRepo.deleteAll();
            appRepo.deleteAll();
            animalRepo.deleteAll();
            userRepo.deleteAll();
            shelterRepo.deleteAll();

            for (String t : new String[]{"shelters","users","animals","applications","analytics_reports"}) {
                jdbc.execute("ALTER TABLE " + t + " ALTER COLUMN id RESTART WITH 1");
            }

            Shelter shelter = new Shelter();
            shelter.setName("Sunshine Animal Rescue");
            shelter.setAddress("ul. Słoneczna 12, Warsaw");
            shelter.setContactEmail("contact@sunshine.org");
            shelter.setContactPhone("+48221234567");
            shelter = shelterRepo.save(shelter);

            User admin = new User();
            admin.setEmail("admin@lpfap.pl");
            admin.setPasswordHash(passwordEncoder.encode("Admin1234!"));
            admin.setFullName("Admin User");
            admin.setPhone("+48600100200");
            admin.setRole(Role.ADMIN);
            admin.setShelter(shelter);
            admin = userRepo.save(admin);

            User jan = new User();
            jan.setEmail("jan.kowalski@example.com");
            jan.setPasswordHash(passwordEncoder.encode("User1234!"));
            jan.setFullName("Jan Kowalski");
            jan.setPhone("+48123456789");
            jan.setRole(Role.USER);
            jan = userRepo.save(jan);

            User anna = new User();
            anna.setEmail("anna.nowak@example.com");
            anna.setPasswordHash(passwordEncoder.encode("User1234!"));
            anna.setFullName("Anna Nowak");
            anna.setPhone("+48987654321");
            anna.setRole(Role.USER);
            anna = userRepo.save(anna);

            Animal buddy = makeAnimal("Buddy", Species.DOG, "Golden Retriever", 24, Sex.MALE,
                    "Friendly and energetic dog, loves walks and cuddles. Knows basic commands.",
                    "Vaccinated & Neutered",
                    "/img/GettyImages-598175960-cute-dog-headshot.jpg",
                    AnimalStatus.AVAILABLE, shelter);
            buddy = animalRepo.save(buddy);

            Animal luna = makeAnimal("Luna", Species.CAT, "Siamese Mix", 6, Sex.FEMALE,
                    "Calm and affectionate cat, great with children and other pets.",
                    "Vaccinated, spayed",
                    "/img/Cat-on-couch.jpg",
                    AnimalStatus.AVAILABLE, shelter);
            luna = animalRepo.save(luna);

            Animal oliver = makeAnimal("Oliver", Species.RABBIT, "Mini Lop", 12, Sex.MALE,
                    "Gentle rabbit, loves hay and exploring. Very social.",
                    "Healthy, regular check-ups done",
                    "/img/coniglietto_appartamento-cure.jpg",
                    AnimalStatus.AVAILABLE, shelter);
            oliver = animalRepo.save(oliver);

            Animal cooper = makeAnimal("Cooper", Species.DOG, "Beagle", 48, Sex.MALE,
                    "Playful beagle, excellent with other dogs. Loves outdoor adventures.",
                    "Vaccinated, microchipped",
                    "/img/cute-happy-pup.jpeg",
                    AnimalStatus.AVAILABLE, shelter);
            cooper = animalRepo.save(cooper);

            Animal misty = makeAnimal("Misty", Species.CAT, "Domestic Shorthair", 36, Sex.FEMALE,
                    "Independent but sweet cat, prefers quiet homes. Loves window seats.",
                    "Vaccinated, spayed",
                    "/img/GettyImages-953005498-4ca60c6c2c5f4b0e881b2746ad5f17ef.jpg",
                    AnimalStatus.AVAILABLE, shelter);
            misty = animalRepo.save(misty);

            Animal bella = makeAnimal("Bella", Species.DOG, "Labrador Mix", 18, Sex.FEMALE,
                    "Sweet and loyal, loves swimming and fetch. Great with children.",
                    "Vaccinated & Neutered",
                    "/img/adorable-brown-white-basenji-dog-smiling-giving-high-five-isolated-white_346278-1657.avif",
                    AnimalStatus.AVAILABLE, shelter);
            bella = animalRepo.save(bella);

            Animal max = makeAnimal("Max", Species.DOG, "German Shepherd", 36, Sex.MALE,
                    "Intelligent and loyal, well-trained. Excellent guard dog and family companion.",
                    "Vaccinated, microchipped, neutered",
                    "/img/download.jpeg",
                    AnimalStatus.AVAILABLE, shelter);
            max = animalRepo.save(max);

            Animal whiskers = makeAnimal("Whiskers", Species.CAT, "Persian Mix", 24, Sex.MALE,
                    "Fluffy and gentle, loves being brushed. Very calm temperament.",
                    "Vaccinated, neutered",
                    "/img/spruce-pets-cutest-cats-16384c1bdb824b1d87861d62b2a73f4c.jpg",
                    AnimalStatus.AVAILABLE, shelter);
            whiskers = animalRepo.save(whiskers);

            Animal daisy = makeAnimal("Daisy", Species.RABBIT, "Holland Lop", 8, Sex.FEMALE,
                    "Playful young rabbit, loves vegetables and hopping around.",
                    "Healthy, first vaccinations done",
                    "/img/Image-A.webp",
                    AnimalStatus.AVAILABLE, shelter);
            daisy = animalRepo.save(daisy);

            Animal charlie = makeAnimal("Charlie", Species.DOG, "Cocker Spaniel", 30, Sex.MALE,
                    "Affectionate spaniel, loves cuddles and playtime.",
                    "Vaccinated & Neutered",
                    "/img/eyJidWNrZXQiOiJjb250ZW50Lmhzd3N0YXRpYy5jb20iLCJrZXkiOiJnaWZcL2dldHR5aW1hZ2VzLTE0MTc5NTY1MDMuanBnIiwiZWRpdHMiOnsicmVzaXplIjp7IndpZHRoIjo4Mjh9fX0=.webp",
                    AnimalStatus.FOSTERED, shelter);
            charlie = animalRepo.save(charlie);

            Application app1 = new Application();
            app1.setType(ApplicationType.ADOPTION);
            app1.setMotivationText("I have a large home with a garden and lots of experience with dogs. " +
                    "I have owned dogs for over 15 years and Buddy would have a wonderful forever home.");
            app1.setApplicant(jan);
            app1.setAnimal(buddy);
            app1.setSubmissionDate(LocalDateTime.now().minusDays(5));
            appRepo.save(app1);

            Application app2 = new Application();
            app2.setType(ApplicationType.FOSTER);
            app2.setMotivationText("I would love to foster Luna temporarily while she finds a permanent home. " +
                    "I have two calm cats already and plenty of space for another feline friend.");
            app2.setApplicant(jan);
            app2.setAnimal(luna);
            app2.setSubmissionDate(LocalDateTime.now().minusDays(20));
            app2.approve();
            luna.markFostered();
            animalRepo.save(luna);
            appRepo.save(app2);

            Application app3 = new Application();
            app3.setType(ApplicationType.ADOPTION);
            app3.setMotivationText("I have a quiet home and experience with small animals including rabbits. " +
                    "I would provide Oliver with excellent care and a loving environment to thrive.");
            app3.setApplicant(jan);
            app3.setAnimal(oliver);
            app3.setSubmissionDate(LocalDateTime.now().minusDays(30));
            app3.reject();
            appRepo.save(app3);

            Application app4 = new Application();
            app4.setType(ApplicationType.ADOPTION);
            app4.setMotivationText("We are an active family with a large yard, perfect for Cooper. " +
                    "We have experience with beagles and can provide the exercise and stimulation he needs.");
            app4.setApplicant(anna);
            app4.setAnimal(cooper);
            app4.setSubmissionDate(LocalDateTime.now().minusDays(3));
            appRepo.save(app4);

            Application app5 = new Application();
            app5.setType(ApplicationType.FOSTER);
            app5.setMotivationText("I am a dog trainer with 10 years of experience. I can provide Bella " +
                    "with excellent care and training while she finds her forever home.");
            app5.setApplicant(anna);
            app5.setAnimal(bella);
            app5.setSubmissionDate(LocalDateTime.now().minusDays(1));
            appRepo.save(app5);

            System.out.println("=================================================");
            System.out.println("  LPFAP started successfully!");
            System.out.println("  Open: http://localhost:8080");
            System.out.println("  Admin:  admin@lpfap.pl / Admin1234!");
            System.out.println("  User:   jan.kowalski@example.com / User1234!");
            System.out.println("  User:   anna.nowak@example.com / User1234!");
            System.out.println("  H2:     http://localhost:8080/h2-console");
            System.out.println("=================================================");
        };
    }

    private Animal makeAnimal(String name, Species species, String breed, int age,
                               Sex sex, String desc, String medical, String photo,
                               AnimalStatus status, Shelter shelter) {
        Animal a = new Animal();
        a.setName(name);
        a.setSpecies(species);
        a.setBreed(breed);
        a.setAge(age);
        a.setSex(sex);
        a.setDescription(desc);
        a.setMedicalStatus(medical);
        a.setPhotoUrl(photo);
        a.setStatus(status);
        a.setShelter(shelter);
        a.setIntakeDate(LocalDate.now().minusDays((long)(Math.random() * 90)));
        return a;
    }
}
