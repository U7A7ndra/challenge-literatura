package com.alura.literatura;

import com.alura.literatura.Models.Autor;
import com.alura.literatura.Models.Libro;
import com.alura.literatura.Models.LibrosRespuestaApi;
import com.alura.literatura.Models.Records.DatosLibro;
import com.alura.literatura.Repositories.IAutorRepository;
import com.alura.literatura.Repositories.ILibroRepository;
import com.alura.literatura.Utils.ConsumoApiGutendex;
import com.alura.literatura.Utils.ConvertirDatos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@SpringBootApplication
public class Application {

	private static final String API_BASE = "https://gutendex.com/books/?search=";

	private final Scanner sc = new Scanner(System.in);
	private final ConsumoApiGutendex consumodeApi = new ConsumoApiGutendex();
	private final ConvertirDatos convertir = new ConvertirDatos();
	private final ILibroRepository libroRepository;
	private final IAutorRepository autorRepository;

	@Autowired
	public Application(ILibroRepository libroRepository, IAutorRepository autorRepository) {
		this.libroRepository = libroRepository;
		this.autorRepository = autorRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Transactional(readOnly = true)
	public void consumo() {
		int opcion = -1;
		while (opcion != 0) {
			String menu = """
                    |***************************************************|
                    |*****       BIENVENIDO A LA LIBRERIA       ******|
                    |***************************************************|
                    
                    1 - Agregar Libro por Nombre
                    2 - Libros buscados
                    3 - Buscar libro por Nombre
                    4 - Buscar todos los Autores de libros buscados
                    5 - Buscar Autores por año
                    6 - Buscar Libros por Idioma
                    7 - Top 10 Libros mas Descargados
                    8 - Buscar Autor por Nombre
                    
                    0 - Salir
                    
                    |***************************************************|
                    |*****            INGRESE UNA OPCIÓN          ******|
                    |***************************************************|
                    """;

			try {
				System.out.println(menu);
				opcion = sc.nextInt();
				sc.nextLine();
			} catch (InputMismatchException e) {
				System.out.println("|****************************************|");
				System.out.println("|  Por favor, ingrese un número válido.  |");
				System.out.println("|****************************************|\n");
				sc.nextLine();
				continue;
			}

			switch (opcion) {
				case 1 -> buscarLibroEnLaWeb();
				case 2 -> librosBuscados();
				case 3 -> buscarLibroPorNombre();
				case 4 -> BuscarAutores();
				case 5 -> buscarAutoresPorAnio();
				case 6 -> buscarLibrosPorIdioma();
				case 7 -> top10LibrosMasDescargados();
				case 8 -> buscarAutorPorNombre();
				case 0 -> {
					System.out.println("|********************************|");
					System.out.println("|    Aplicación cerrada. Adios!    |");
					System.out.println("|********************************|\n");
				}
				default -> {
					System.out.println("|*********************|");
					System.out.println("|  Opción Incorrecta. |");
					System.out.println("|*********************|\n");
					consumo();
				}
			}
		}
	}

	private Libro getDatosLibro() {
		System.out.println("Ingrese el nombre del libro: ");
		String nombreLibro = sc.nextLine().toLowerCase();
		String json = consumodeApi.obtenerDatos(API_BASE + nombreLibro.replace(" ", "%20"));
		LibrosRespuestaApi datos = convertir.convertirDatosJsonAJava(json, LibrosRespuestaApi.class);

		if (datos != null && datos.getResultadoLibros() != null && !datos.getResultadoLibros().isEmpty()) {
			DatosLibro primerLibro = datos.getResultadoLibros().get(0);
			return new Libro(primerLibro);
		} else {
			System.out.println("No se encontraron resultados.");
			return null;
		}
	}

	private void buscarLibroEnLaWeb() {
		Libro libro = getDatosLibro();

		if (libro == null) {
			System.out.println("Libro no encontrado.");
			return;
		}

		try {
			boolean libroExists = libroRepository.existsByTitulo(libro.getTitulo());
			if (libroExists) {
				System.out.println("El libro ya existe en la base de datos!");
			} else {
				libroRepository.save(libro);
				System.out.println(libro);
			}
		} catch (InvalidDataAccessApiUsageException e) {
			System.out.println("No se puede persistir el libro buscado!");
		}
	}

	@Transactional(readOnly = true)
	private void librosBuscados() {
		List<Libro> libros = libroRepository.findAll();
		if (libros.isEmpty()) {
			System.out.println("No se encontraron libros en la base de datos.");
		} else {
			libros.forEach(System.out::println);
		}
	}

	private void buscarLibroPorNombre() {
		System.out.println("Ingrese Titulo libro que quiere buscar: ");
		String titulo = sc.nextLine();
		Libro libroBuscado = libroRepository.findByTituloContainsIgnoreCase(titulo);
		if (libroBuscado != null) {
			System.out.println("El libro buscado fue: " + libroBuscado);
		} else {
			System.out.println("El libro con el titulo '" + titulo + "' no se encontró.");
		}
	}

	private void BuscarAutores() {
		List<Autor> autores = autorRepository.findAll();
		if (autores.isEmpty()) {
			System.out.println("No se encontraron autores.");
		} else {
			Set<String> autoresUnicos = new HashSet<>();
			autores.forEach(autor -> {
				if (autoresUnicos.add(autor.getNombre())) {
					System.out.println(autor.getNombre());
				}
			});
		}
	}

	private void buscarLibrosPorIdioma() {
		System.out.println("Ingrese Idioma en el que quiere buscar: ");
		System.out.println("|***********************************|");
		System.out.println("|  Opción - es : Libros en español. |");
		System.out.println("|  Opción - en : Libros en ingles.  |");
		System.out.println("|***********************************|");

		String idioma = sc.nextLine();
		List<Libro> librosPorIdioma = libroRepository.findByIdioma(idioma);

		if (librosPorIdioma.isEmpty()) {
			System.out.println("No se encontraron libros en la base de datos.");
		} else {
			librosPorIdioma.forEach(System.out::println);
		}
	}

	private void buscarAutoresPorAnio() {
		System.out.println("Indica el año para consultar que autores están vivos: ");
		int anioBuscado = sc.nextInt();
		sc.nextLine();

		List<Autor> autoresVivos = autorRepository.findByCumpleaniosLessThanOrFechaFallecimientoGreaterThanEqual(anioBuscado, anioBuscado);

		if (autoresVivos.isEmpty()) {
			System.out.println("No se encontraron autores que estuvieran vivos en el año " + anioBuscado + ".");
		} else {
			Set<String> autoresUnicos = new HashSet<>();
			autoresVivos.forEach(autor -> {
				if (autor.getCumpleanios() != null && autor.getFechaFallecimiento() != null) {
					if (autor.getCumpleanios() <= anioBuscado && autor.getFechaFallecimiento() >= anioBuscado) {
						if (autoresUnicos.add(autor.getNombre())) {
							System.out.println("Autor: " + autor.getNombre());
						}
					}
				}
			});
		}
	}

	private void top10LibrosMasDescargados() {
		List<Libro> top10Libros = libroRepository.findTop10ByTituloByCantidadDescargas();
		if (!top10Libros.isEmpty()) {
			int index = 1;
			for (Libro libro : top10Libros) {
				System.out.printf("Libro %d: %s Autor: %s Descargas: %d\n",
						index, libro.getTitulo(), libro.getAutores().getNombre(), libro.getCantidadDescargas());
				index++;
			}
		}
	}

	private void buscarAutorPorNombre() {
		System.out.println("Ingrese nombre del escritor que quiere buscar: ");
		String escritor = sc.nextLine();
		Optional<Autor> escritorBuscado = autorRepository.findFirstByNombreContainsIgnoreCase(escritor);
		escritorBuscado.ifPresentOrElse(
				autor -> System.out.println("El escritor buscado fue: " + autor.getNombre()),
				() -> System.out.println("El escritor con el nombre '" + escritor + "' no se encontró.")
		);
	}
}
