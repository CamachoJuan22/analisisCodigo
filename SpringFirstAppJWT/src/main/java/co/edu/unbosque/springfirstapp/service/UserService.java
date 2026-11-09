package co.edu.unbosque.springfirstapp.service;

import co.edu.unbosque.springfirstapp.dto.UserDTO;
import co.edu.unbosque.springfirstapp.model.User;
import co.edu.unbosque.springfirstapp.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio que proporciona operaciones CRUD y funcionalidades adicionales para usuarios. Implementa
 * la interfaz CRUDOperation para operaciones básicas de creación, lectura, actualización y
 * eliminación.
 */
@Service
public class UserService implements CRUDOperation<UserDTO> {

  /** Repositorio para acceder a los datos de usuarios en la base de datos. */
  @Autowired private UserRepository userRepo;

  /** Mapeador para convertir entre entidades User y DTOs UserDTO. */
  @Autowired private ModelMapper modelMapper;

  /** Codificador para encriptar contraseñas de usuarios. */
  @Autowired private PasswordEncoder passwordEncoder;

  /** Constructor por defecto. */
  public UserService() {}

  /**
   * Cuenta el número total de usuarios en la base de datos.
   *
   * @return Número total de usuarios
   */
  @Override
  public long count() {
    return userRepo.count();
  }

  /**
   * Verifica si existe un usuario con el ID especificado.
   *
   * @param id ID del usuario a verificar
   * @return true si el usuario existe, false en caso contrario
   */
  @Override
  public boolean exist(Long id) {
    return userRepo.existsById(id);
  }

  /**
   * Crea un nuevo usuario en la base de datos.
   * Convierte el DTO en una entidad, encripta la contraseña y
   * guarda los datos del usuario, incluyendo su edad y fecha de nacimiento.
   *
   * @param data DTO con los datos del usuario a crear.
   * @return 0 si la creación fue exitosa, 1 si el nombre de usuario ya existe.
   */
  @Override
  public int create(UserDTO data) {
      // Convertir el DTO recibido en una entidad User.
      // ModelMapper transfiere también edad y fechaNacimiento
      // si existen en ambas clases con sus respectivos getters y setters.
      User entity = modelMapper.map(data, User.class);

      // Verificar si el nombre de usuario ya está registrado.
      if (findUsernameAlreadyTaken(entity)) {
          return 1;
      } else {
          // Encriptar la contraseña antes de guardar el usuario.
          entity.setPassword(passwordEncoder.encode(entity.getPassword()));

          // Establecer el rol si se proporciona.
          // Si no se proporciona, se conserva el rol predeterminado USER.
          if (data.getRole() != null) {
              entity.setRole(data.getRole());
          }

          // Guardar el usuario con su edad y fecha de nacimiento.
          userRepo.save(entity);

          return 0;
      }
  }


  /**
   * Obtiene todos los usuarios de la base de datos.
   *
   * @return Lista de DTOs de usuarios
   */
  @Override
  public List<UserDTO> getAll() {
    List<User> entityList = userRepo.findAll();
    List<UserDTO> dtoList = new ArrayList<>();
    entityList.forEach(
        (entity) -> {
          UserDTO dto = modelMapper.map(entity, UserDTO.class);
          dtoList.add(dto);
        });

    return dtoList;
  }

  /**
   * Elimina un usuario por su ID.
   *
   * @param id ID del usuario a eliminar
   * @return 0 si la eliminación fue exitosa, 1 si el usuario no existe
   */
  @Override
  public int deleteById(Long id) {
    Optional<User> found = userRepo.findById(id);
    if (found.isPresent()) {
      userRepo.delete(found.get());
      return 0;
    } else {
      return 1;
    }
  }

  /**
   * Elimina un usuario por su nombre de usuario.
   *
   * @param username Nombre de usuario del usuario a eliminar
   * @return 0 si la eliminación fue exitosa, 1 si el usuario no existe
   */
  public int deleteByUsername(String username) {
    Optional<User> found = userRepo.findByUsername(username);
    if (found.isPresent()) {
      userRepo.delete(found.get());
      return 0;
    } else {
      return 1;
    }
  }

  /**
   * Actualiza un usuario existente por su ID.
   * Actualiza el nombre de usuario, la contraseña, el rol,
   * la edad y la fecha de nacimiento.
   *
   * @param id ID del usuario que se desea actualizar.
   * @param newData DTO con los nuevos datos del usuario.
   * @return 0 si la actualización fue exitosa,
   *         1 si el nombre de usuario ya está en uso por otro usuario,
   *         2 si el usuario que se desea actualizar no existe,
   *         3 en otros casos de error.
   */
  @Override
  public int updateById(Long id, UserDTO newData) {
      // Buscar el usuario que se desea actualizar.
      Optional<User> found = userRepo.findById(id);

      // Buscar si el nuevo nombre de usuario ya está registrado.
      Optional<User> newFound =
              userRepo.findByUsername(newData.getUsername());

      // Verificar si existe el usuario y si el nombre de usuario
      // no pertenece a otro registro.
      if (found.isPresent()
              && (!newFound.isPresent()
              || newFound.get().getId().equals(id))) {

          // Obtener la entidad existente.
          User temp = found.get();

          // Actualizar el nombre de usuario.
          temp.setUsername(newData.getUsername());

          // Encriptar y actualizar la contraseña.
          temp.setPassword(
                  passwordEncoder.encode(newData.getPassword()));

          // Actualizar el rol únicamente si se proporciona.
          if (newData.getRole() != null) {
              temp.setRole(newData.getRole());
          }

          // Actualizar la edad.
          temp.setEdad(newData.getEdad());

          // Actualizar la fecha de nacimiento.
          temp.setFechaNacimiento(newData.getFechaNacimiento());

          // Guardar los cambios en la base de datos.
          userRepo.save(temp);

          return 0;
      }

      // El nombre de usuario pertenece a otro usuario.
      if (found.isPresent() && newFound.isPresent()) {
          return 1;
      }

      // El usuario que se desea actualizar no existe.
      if (!found.isPresent()) {
          return 2;
      }

      // Caso de error no contemplado.
      return 3;
  }

  /**
   * Obtiene un usuario por su ID.
   *
   * @param id ID del usuario a obtener
   * @return DTO del usuario si existe, null en caso contrario
   */
  public UserDTO getById(Long id) {
    Optional<User> found = userRepo.findById(id);
    if (found.isPresent()) {
      return modelMapper.map(found.get(), UserDTO.class);
    } else {
      return null;
    }
  }

  /**
   * Verifica si un nombre de usuario ya está en uso.
   *
   * @param newUser Usuario con el nombre de usuario a verificar
   * @return true si el nombre de usuario ya está en uso, false en caso contrario
   */
  public boolean findUsernameAlreadyTaken(User newUser) {
    Optional<User> found = userRepo.findByUsername(newUser.getUsername());
    if (found.isPresent()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Verifica si un nombre de usuario ya está en uso.
   *
   * @param username Nombre de usuario a verificar
   * @return true si el nombre de usuario ya está en uso, false en caso contrario
   */
  public boolean findUsernameAlreadyTaken(String username) {
    Optional<User> found = userRepo.findByUsername(username);
    return found.isPresent();
  }

  /**
   * Valida las credenciales de un usuario.
   *
   * @param username Nombre de usuario
   * @param password Contraseña sin encriptar
   * @return 0 si las credenciales son válidas, 1 si son inválidas
   */
  public int validateCredentials(String username, String password) {
    // Buscar usuario por nombre de usuario
    Optional<User> userOpt = userRepo.findByUsername(username);

    // Verificar si el usuario existe y la contraseña coincide
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      if (passwordEncoder.matches(password, user.getPassword())) {
        return 0; // Éxito
      }
    }

    return 1; // Credenciales inválidas
  }
}
