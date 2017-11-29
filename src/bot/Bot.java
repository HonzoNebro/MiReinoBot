package bot;

import java.util.ArrayList;
import java.util.Collections;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

public class Bot extends TelegramLongPollingBot {

	@Override
	public String getBotUsername() {
		return "Joderconelbot";
	}

	@Override
	public void onUpdateReceived(Update arg0) {

		// almacenar ID del remitente
		SendMessage sendMessage = new SendMessage().setChatId(arg0.getMessage().getChatId());

		// imprimir por consola el mensaje recibido
		System.out.println("\n" + arg0.getMessage().getFrom().getFirstName() + ": " + arg0.getMessage().getText());

		// si el mensaje es un comando asume que va a ser una tirada con formato: /1d20
		if (arg0.getMessage().getText().startsWith("/")) {
			String mensaje = arg0.getMessage().getText().substring(1);
			discernirComando(arg0.getMessage().getText().substring(1));

			// prueba de expresión regular
			/*
			 * if(mensaje.matches("\\d++[d]\\d++([+-]\\d++)*")) {
			 * System.out.println("es una tirada!!"); }
			 */

			int numDados = 0;
			int numCaras = 2;
			int valorBonificador = 0;
			int sumaTotal = 0;
			// separa las partes numéricas
			try {
				String[] partesNumericas = arg0.getMessage().getText().split("d");
				numDados = Integer.parseInt(partesNumericas[0].substring(1));
				numCaras = Integer.parseInt(partesNumericas[1]);

				if (mensaje.contains("+")) {
					valorBonificador = Integer.parseInt(mensaje.substring(mensaje.indexOf("+")));
					System.out.println("bonificador: " + valorBonificador);
				} else if (mensaje.contains("-")) {
					valorBonificador = Integer.parseInt(mensaje.substring(mensaje.indexOf("-")));
					System.out.println("bonificador: " + valorBonificador);
				}
				System.out.println("Sin bonificador que mostrar");

				if ((numDados <= 0) || (numDados > 50)) {
					sendMessage.setText("No puedo tirar esa cantidad de dados. Máximo 50 dados");
				} else if (numCaras <= 1) {
					sendMessage.setText("¿Que dado conoces con una sola cara?");
				} else {
					// System.out.println(numDados + " dado/s de: " + numCaras + " caras");
					ArrayList<Integer> dados = new ArrayList<Integer>();

					// calcula el valor
					for (int i = 0; i < numDados; i++) {
						int valor = (int) (Math.random() * numCaras + 1);
						dados.add(valor);
						sumaTotal += valor;
					}
					// ordena los dados de menor a mayor
					Collections.sort(dados);

					// prepara el mensaje de respuesta
					sendMessage.setText("[" + numDados + "d" + numCaras + "]->" + dados + " = " + sumaTotal);
				}

			} catch (Exception e) {
				// en caso contrario responde con un mensaje, ayudando a usar el bot
				sendMessage.setText("Hola " + arg0.getMessage().getFrom().getFirstName()
						+ " , Por ahora solo respondo a la tirada de dados, ejemplo: /1d20");
			}

		} else {
			// en caso contrario responde con un mensaje, ayudando a usar el bot
			sendMessage.setText("Hola " + arg0.getMessage().getFrom().getFirstName()
					+ " , Por ahora solo respondo a la tirada de dados, ejemplo: /1d20");
		}

		try {
			// System.out.println("Respuesta del bot: " + sendMessage.getText());
			sendMessage(sendMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getBotToken() {
		// TODO Auto-generated method stub
		return "491553940:AAG8_u2AeeHOMQExf7RRgAsQnnkKNSjL9OY";
	}

	public void discernirComando(String comando) {
		// Dado único
		if (comando.matches("[d]\\d+")) {
			// d6
			// es una tirada de un solo dado
		} else if (comando.matches("[d]\\d+[!]")) {
			// d6!
			// es una tirada de un solo dado explosivo
		} else if (comando.matches("[d]\\d+[+-]\\d+")) {
			// d5+6 d3-21
			// es una tirada de un dado con modificador
		} else if (comando.matches("[d]\\d+[!][+-]\\d+")) {
			// d5!+6 d3!+21
			// es una tirada de un dado explosivo con modificador
		}

		// Varios dados
		else if (comando.matches("\\d+[d]\\d+")) {
			// 2d6
			// es una tirada de varios dados
		} else if (comando.matches("\\d+[d]\\d+[!]")) {
			// 2d6!
			// es una tirada de varios dados explosivos
		} else if (comando.matches("\\d+[d]\\d+[+-]\\d+")) {
			// 3d5+6 10d3-21
			// es una tirada de varios dados con modificador
		} else if (comando.matches("\\d+[d]\\d+[!][+-]\\d+")) {
			// 3d5!+6 10d3!-21
			// es una tirada de varios dados explosivos con modificador
		}

		// Tirada múltiple de un solo dado
		else if (comando.matches("\\d+[x][d]\\d+")) {
			// 3xd5
			// es una tirada multiple, de 1d5 cada una
		} else if (comando.matches("\\d+[x][d]\\d+[!]")) {
			// 3xd5!
			// es una tirada multiple, de 1d5 explosivo cada una
		} else if (comando.matches("\\d+[x][d]\\d+[+-]\\d+")) {
			// 3xd5+10
			// es una tirada multiple, de 1d5 cada una
			// donde aplicamos modificador
		} else if (comando.matches("\\d+[x][d]\\d+[!][+-]\\d+")) {
			// 3xd5!+10
			// es una tirada multiple, de 1d5 explosivo cada una
			// donde aplicamos modificador
		}

		// Tirada múltiple de varios dados
		else if (comando.matches("\\d+[x]\\d+[d]\\d+")) {
			// 6x4d6
			// es una tirada multiple, de 4d6 cada una
		} else if (comando.matches("\\d+[x]\\d+[d]\\d+[!]")) {
			// 6x4d6!
			// es una tirada multiple, de 4d6 explosivos cada una
		} else if (comando.matches("\\d+[x]\\d+[d]\\d+[+-]\\d+")) {
			// 3x2d5+10
			// es una tirada multiple, de 2d5 cada una
			// donde aplicamos modificador
		} else if (comando.matches("\\d+[x]\\d+[d]\\d+[!][+-]\\d+")) {
			// 3x2d5!+10
			// es una tirada multiple, de 2d5 explosivos cada una
			// donde aplicamos modificador
		}

		// Tirada de varios dados, donde guardamos dados **nECESITA REPLANTEARSE**
		else if (comando.matches("\\d+[d]\\d+[hlHL]\\d+")) {
			// 3d5h1
			// es una tirada, de 3d5 donde guardamos dados
		} else if (comando.matches("\\d+[d]\\d+[hlHL]\\d+")) {
			// 3d5!h1
			// es una tirada, de 3d5 explosivos donde guardamos dados
		} else if (comando.matches("\\d+[d]\\d+[hlHL]\\d+[+-]\\d+")) {
			// 3d5l1+5
			// es una tirada, de 3d5 donde guardamos dados y
			// aplicamos el modificador
		} else if (comando.matches("\\d+[d]\\d+[hlHL]\\d+[+-]\\d+")) {
			// 3d5!l1+5
			// es una tirada, de 3d5 explosivos donde guardamos dados y
			// aplicamos el modificador
		}

		// Tirada multiple de varios dados donde guardamos dados **NECESITA
		// REPLANTEARSE**
		else if (comando.matches("\\d+[x]\\d+[d]\\d+[hlHL]\\d+")) {
			// 6x4d6h3
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
		} else if (comando.matches("\\d+[x]\\d+[d]\\d+[!][hlHL]\\d+")) {
			// 6x4d6!h3
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
		} else if (comando.matches("\\d+[x]\\d+[d]\\d+[hlHL]\\d+[+-]\\d+")) {
			// 6x4d6l3+5
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
			// y aplicamos el modificador
		} else if (comando.matches("\\d+[x]\\d+[d]\\d+[!][hlHL]\\d+[+-]\\d+")) {
			// 6x4d6!l3+5
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
			// y aplicamos el modificador
		}

		// Tirada de Fate
		else if (comando.matches("\\b(fate|Fate|FATE)")) {
			// es una tirada de Fate
		} else if (comando.matches("\\b(fate|Fate|FATE)[+-]\\d+")) {
			// esta es una tirada fate con modificador
		}

		// Tirada de Hitos
		else if (comando.matches("\\b(hitos|Hitos|HITOS)")) {
			// esta es una tirada Hitos con modificador
		} else if (comando.matches("\\b(hitos|Hitos|HITOS)[+-]\\d+")) {
			// esta es una tirada Hitos con modificador
		}
		/*
		 * TODO
		 * 
		 * higher y lower por encima de la cantidad de dados o 0 dados se cogen todos y
		 * ya 3d5l0 3d5h10 
		 * l y h pasan a DropLowest y KeepLowest (dl, kl) y DropHighest
		 * y Keephighest (dh, kh)
		 * 
		 * expresiones regulares por detectar
		 * 
		 * tiradas shadowrun
		 * 
		 * tiradas hackmaster/dados penetrantes (5d6!p)
		 * cualquier tirada que hagas, si sacas el máximo en el dado vuelves a tirar y sumas
		 * (con un -1 al nuevo dado) y si vuelves a sacar otra vez el máximo vuelves a tirar... 
		 * 
		 * tiradas vampiro (dificultad 3d5>6)

		 * reroll (relanzar) 3d6r<3
		 * 
		 * agrupar
		 * 
		 * texto (3d10 Tirada hitos)
		 * sladría algo tipo 4,4,6 Tirada hitos
		 * 
		 * https://wiki.roll20.net/Dice_Reference
		 * 
		 */
	}

	public void lanzarFate() {

	}

}
