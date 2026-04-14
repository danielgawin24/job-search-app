package com.jsa.jobsearchapp.user;

import com.jsa.jobsearchapp.exception.InvalidRequestBodyException;
import com.jsa.jobsearchapp.exception.UserAlreadyRegisteredException;
import com.jsa.jobsearchapp.jobOffer.EmploymentType;
import com.jsa.jobsearchapp.jobOffer.Seniority;
import com.jsa.jobsearchapp.jobOffer.TypeOfContract;
import com.jsa.jobsearchapp.request.RegisterRequest;
import com.jsa.jobsearchapp.request.UserPrefRequest;
import com.jsa.jobsearchapp.skill.Skill;
import com.jsa.jobsearchapp.skill.SkillRepository;
import com.jsa.jobsearchapp.userPref.UserPref;
import com.jsa.jobsearchapp.userPref.UserPrefDTO;
import com.jsa.jobsearchapp.userPref.UserPrefRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserPrefRepository userPrefRepository;
    private final ModelMapper modelMapper;
    private final SkillRepository skillRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, UserPrefRepository userPrefRepository, ModelMapper modelMapper, SkillRepository skillRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userPrefRepository = userPrefRepository;
        this.modelMapper = modelMapper;
        this.skillRepository = skillRepository;
    }

    public String registerUser(RegisterRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isPresent()) {
            throw new UserAlreadyRegisteredException("User already registered with email: " + user.get().getEmail());
        }
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setUsername(request.getUsername());
        String passwordHash = passwordEncoder.encode(request.getPassword());
        newUser.setPassword(passwordHash);
        newUser.setRole(UserRole.USER);
        userRepository.save(newUser);
        return "User created with email " + newUser.getEmail();
    }

    public String deleteAllUsersExceptAdmin() {
        List<User> usersExceptAdmin = userRepository.findAllByRole(UserRole.USER);
        userRepository.deleteAll(usersExceptAdmin);
        return "Deleted " + usersExceptAdmin.size() + " user(s)";
    }

    public UserPrefDTO getPreferences(String userUsername) {
        User user = userRepository.findByUsername(userUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userUsername));
        UserPref userPref = user.getUserPref();
        if (userPref == null) {
            UserPref newUserPref = new UserPref();
            userPrefRepository.save(newUserPref);
            user.setUserPref(newUserPref);
            userRepository.save(user);
            return modelMapper.map(user.getUserPref(), UserPrefDTO.class);
        }
        return modelMapper.map(user.getUserPref(), UserPrefDTO.class);
    }

    public UserPrefDTO putPreferences(String username, UserPrefRequest request) {
        if (request.isAnyFieldNull()) {
            throw new InvalidRequestBodyException("");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        UserPref userPref = userPrefRepository.findByUser(user).orElse(new UserPref());
        userPref.setCity(request.getCity());
        userPref.setSeniority(Seniority.valueOf(request.getSeniority().toString().toUpperCase()));
        userPref.setSalaryFrom(request.getSalaryFrom());
        userPref.setSalaryTo(request.getSalaryTo());
        userPref.setEmploymentType(EmploymentType.valueOf(request.getEmploymentType().toString().toUpperCase()));
        userPref.setTypeOfContract(TypeOfContract.valueOf(request.getTypeOfContract().toString().toUpperCase()));
        userPref.setWorkModes(request.getWorkModes());
        Set<Skill> userPrefSkillsSet = new HashSet<>();
        for (String skillName : request.getSkills()) {
            skillRepository.findByAliasName(skillName).ifPresent(userPrefSkillsSet::add);
        }
        userPref.setUserPrefSkills(userPrefSkillsSet);
        userPref.setPriorityColumn(request.getPriorityColumn());
        userPref.setIsNoLocationPref(request.getIsNoLocationPref());

        userPrefRepository.save(userPref);
        user.setUserPref(userPref);
        userRepository.save(user);
        return modelMapper.map(user.getUserPref(), UserPrefDTO.class);
    }

    public UserPrefDTO patchPreferences(String username, UserPrefRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        UserPref userPref = userPrefRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("UserPref not found: " + username + ". Please create one first."));

        if (request.getCity() != null) {
            userPref.setCity(request.getCity());
        }
        if (request.getSeniority() != null) {
            userPref.setSeniority(request.getSeniority());
        }
        if (request.getSalaryFrom() != null) {
            userPref.setSalaryFrom(request.getSalaryFrom());
        }
        if (request.getSalaryTo() != null) {
            userPref.setSalaryTo(request.getSalaryTo());
        }
        if (request.getEmploymentType() != null) {
            userPref.setEmploymentType(EmploymentType.valueOf(request.getEmploymentType().toString().toUpperCase()));
        }
        if (request.getTypeOfContract() != null) {
            userPref.setTypeOfContract(TypeOfContract.valueOf(request.getTypeOfContract().toString().toUpperCase()));
        }
        if (request.getWorkModes() != null) {
            userPref.setWorkModes(request.getWorkModes());
        }
        if (request.getSkills() != null) {
            Set<Skill> userPrefSkillsSet = new HashSet<>();
            for (String skillName : request.getSkills()) {
                skillRepository.findByAliasName(skillName).ifPresent(userPrefSkillsSet::add);
            }
            userPref.setUserPrefSkills(userPrefSkillsSet);
        }
        if (request.getPriorityColumn() != null) {
            userPref.setPriorityColumn(request.getPriorityColumn());
        }
        if (request.getIsNoLocationPref() != null) {
            userPref.setIsNoLocationPref(request.getIsNoLocationPref());
        }
        userPrefRepository.save(userPref);
        return modelMapper.map(user.getUserPref(), UserPrefDTO.class);
    }
}
