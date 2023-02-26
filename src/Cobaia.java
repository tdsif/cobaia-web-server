import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Cobaia {
  public static void main(String[] args) {

    System.out.println("Vai cobaia! pressione CTRL+C para terminar.");

		CobaiaHTTPServer webServer = new CobaiaHTTPServer();

		webServer.vai();
  }
}

class CobaiaHTTPServer {

	private static final int PORTA = 9999;
	private static final boolean FLAG_IMPRIMIR_HEADERS = true;

	private static final String PAYLOAD_OI =
			"<html><head><title>E ae!</title></head><body>E <b>ae!!!</b> Ça va?</body></html>";

	private static final String PAYLOAD_TCHAU =
			"<html><head><title>Arrivederci!</title></head><body><i>C ya!</i></body></html>";

	public void vai() {
		try (ServerSocket servidor = new ServerSocket(PORTA)) {

			while (true) {
				// chegou requisição:
				Socket cliente = servidor.accept();
				// passa para o método de tratamento:
				atende(cliente);
			}
		} catch (Exception causa) {
			throw new RuntimeException(causa);
		}
	}

	private void atende(Socket cliente) {
		System.out.println("Atendendo cliente ...");
		System.out.println(cliente.getPort());

		try (InputStream input = cliente.getInputStream();
				      Scanner scan = new Scanner(input)) {

			// a primeira linha é a requisição em si:
			String primeiraLinha = null;

			// vai lendo até acabar:
			String linha = "";
			do {
				linha = scan.nextLine();
				if (primeiraLinha == null) primeiraLinha = linha;
				if (FLAG_IMPRIMIR_HEADERS) System.out.println(linha);
			} while ( ! linha.isEmpty());

			System.out.println("Requisitando: " + primeiraLinha);
			// [GET|POST|DELETE|PUT|PATCH] [PATH] HTTP/VERSION
			String[] partes = primeiraLinha.split(" ");

			try (OutputStream output = cliente.getOutputStream();
				   PrintWriter printer = new PrintWriter(output)) {

				if ("GET".equals(partes[0])) {
					if ("/oi".equals(partes[1])) {

						System.out.println("Enviando oi ...");
						// HTTP/VERSION STATUS_CODE STATUS_TEXT
						// HEADERS
						// PAYLOAD
						printer.println("HTTP/1.1 200 OK");
						printer.println("Content-Type: text/html; charset=utf-8");
						printer.println("Server: Cobaia 0.0.1");
						printer.println("Content-Length: " + PAYLOAD_OI.length());
						printer.println();

						printer.println(PAYLOAD_OI);

					} else if ("/tchau".equals(partes[1])) {

						System.out.println("Enviando tchau ...");

						printer.println("HTTP/1.1 200 OK");
						printer.println("Content-Type: text/html");
						printer.println("Server: Cobaia 0.0.1");
						printer.println("Content-Length: " + PAYLOAD_TCHAU.length());
						printer.println();

						printer.println(PAYLOAD_TCHAU);

					} else {

						System.out.println("Enviando não encontrado ...");

						final String naoEncontrado = String.format("Eita, o recurso %s não foi encontrado neste servidor", partes[1]);

						printer.println("HTTP/1.1 404 NOT FOUND");
						printer.println("Content-Type: text/plain");
						printer.println("Server: Cobaia 0.0.1");
						printer.println("Content-Length: " + naoEncontrado.length());

						printer.println();

						printer.println(naoEncontrado);
					}
				} else {

					System.out.println("Enviando método não suportado ...");

					final String naoSuportado = "Este servidor aceita apenas GET";

					printer.println("HTTP/1.1 405 METHOD NOT ALLOWED");
					printer.println("Content-Type: text/plain");
					printer.println("Server: Cobaia 0.0.1");
					printer.println("Content-Length: " + naoSuportado.length());

					printer.println(naoSuportado);

				}

				// FIM DA TRANSMISSÃO DE ENVIO
				printer.println();
				printer.println();

				System.out.println("E foi!\n");
			}
		} catch (Exception causa) {
			throw new RuntimeException(causa);
		}
	}
}
