package com.epam.taskgym;

import com.epam.taskgym.dao.TraineeDAO;
import com.epam.taskgym.dao.TrainerDAO;
import com.epam.taskgym.dao.TrainingTypeDAO;
import com.epam.taskgym.dao.UserDAO;
import com.epam.taskgym.dto.TraineeDTO;
import com.epam.taskgym.dto.TrainerDTO;
import com.epam.taskgym.entity.Trainee;
import com.epam.taskgym.entity.Trainer;
import com.epam.taskgym.entity.TrainingType;
import com.epam.taskgym.entity.User;
import com.epam.taskgym.service.TraineeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

@Component
public class DataLoader {
    private final UserDAO userDAO;
    private final TraineeDAO traineeDAO;
    private final TrainerDAO trainerDAO;
    private final TrainingTypeDAO trainingTypeDAO;

    private TraineeService traineeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    public DataLoader(UserDAO userDAO, TraineeDAO traineeDAO, TrainerDAO trainerDAO, TrainingTypeDAO trainingTypeDAO, TraineeService traineeService) {
        this.userDAO = userDAO;
        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
        this.trainingTypeDAO = trainingTypeDAO;
        this.traineeService = traineeService;
    }

    @PostConstruct
    public void loadData() {
        try {
            File dataFile = Paths.get("src/main/resources/data.json").toFile();
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode jsonNode = objectMapper.readTree(dataFile);
            TraineeDTO[] trainees = objectMapper.convertValue(jsonNode.get("trainees"), TraineeDTO[].class);
            TrainerDTO[] trainers = objectMapper.convertValue(jsonNode.get("trainers"), TrainerDTO[].class);
            TrainingType[] trainingTypes = objectMapper.convertValue(jsonNode.get("trainingTypes"), TrainingType[].class);

            // Continue to the next steps
            for (TraineeDTO traineeDTO : trainees) {
                User user = fillUser(traineeDTO.getFirstName(), traineeDTO.getLastName());
                user = userDAO.save(user);
                LOGGER.info("User saved with ID: {}", user.getId());

                Trainee trainee = new Trainee();
                trainee.setUserId(user.getId());
                trainee.setDateOfBirth(traineeDTO.getDateOfBirth());
                trainee.setAddress(traineeDTO.getAddress());
                trainee = traineeDAO.save(trainee);
                LOGGER.info("Trainee saved with ID: {}", trainee.getId());
            }

            for (TrainerDTO trainerDTO : trainers) {
                User user = fillUser(trainerDTO.getFirstName(), trainerDTO.getLastName());
                user = userDAO.save(user);
                LOGGER.info("User saved with ID: {}", user.getId());;

                Trainer trainer = new Trainer();
                trainer.setUserId(user.getId());
                trainer.setSpecialization(trainerDTO.getSpecialization());
                trainer = trainerDAO.save(trainer);
                LOGGER.info("Trainer saved with ID: {}", trainer.getId());
            }

            for (TrainingType trainingType : trainingTypes) {
                trainingType = trainingTypeDAO.save(trainingType);
                LOGGER.info("trainingType saved with ID: {}", trainingType.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();  // Handle this exception
        }
    }

    private User fillUser(String firstName, String lastName) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        String username = traineeService.generateUniqueUsername(user.getFirstName().toLowerCase(), user.getLastName().toLowerCase());
        user.setUsername(username);
        String password = traineeService.generateRandomPassword();
        user.setPassword(password);
        return user;
    }
}