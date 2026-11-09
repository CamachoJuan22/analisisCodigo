package co.edu.unbosque.springfirstapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.springfirstapp.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad User.
 * Proporciona operaciones CRUD básicas heredadas de JpaRepository
 * y métodos personalizados para buscar y eliminar usuarios por nombre de usuario.
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * Busca un usuario por su nombre de usuario.
	 * 
	 * @param username El nombre de usuario a buscar
	 * @return Un Optional que contiene el usuario si existe, o vacío si no existe
	 */
	public Optional<User> findByUsername(String username);

	/**
	 * Elimina un usuario por su nombre de usuario.
	 * 
	 * @param username El nombre de usuario del usuario a eliminar
	 */
	public void deleteByUsername(String username); 
	
	/**
     * Busca usuarios por su edad.
     *
     * @param edad La edad de los usuarios que se desean buscar.
     * @return Una lista con los usuarios que tienen la edad indicada.
     */
    public List<User> findByEdad(int edad);

    /**
     * Busca usuarios por su fecha de nacimiento.
     *
     * @param fechaNacimiento La fecha de nacimiento que se desea buscar.
     * @return Una lista con los usuarios que nacieron en esa fecha.
     */
    public List<User> findByFechaNacimiento(LocalDate fechaNacimiento);
	
}
