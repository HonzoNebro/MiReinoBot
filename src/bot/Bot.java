package bot;

import java.util.ArrayList;
import java.util.Collections;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {

	int numberOfDice = 0, numberOfSides = 0, rollModifier = 0, rollResult = 0;
	SendMessage sendMessage;

	@Override
	public String getBotUsername() {
		// SUSTITUIR POR EL ALIAS DE TU BOT
		return "NULL";
	}

	@Override
	public void onUpdateReceived(Update arg0) {

		// Almacenar la ID del chat para poder contestar
		sendMessage = new SendMessage().setChatId(arg0.getMessage().getChatId());

		// Imprimir por consola el mensaje recibido
		System.out.println("\nMENSAJE RECIBIDO DE " + arg0.getMessage().getFrom().getFirstName() + ": "
				+ arg0.getMessage().getText());

		// Si el mensaje es un comando, se intenta averiguar cual
		if (arg0.getMessage().getText().startsWith("/")) {
			findCommand(arg0.getMessage().getText().substring(1));

		} else {
			// En caso contrario responde con un mensaje, ayudando a usar el bot
			sendMessage.setText("Hola " + arg0.getMessage().getFrom().getFirstName()
					+ " , Por ahora solo respondo a la tirada de dados, ejemplo: /1d20");
		}

	}

	public void answerUser() {
		// Respuesta del bot centralizada, todas las respuestas se mandan desde aquí
		// también se escribe un mensaje por consola
		try {
			System.out.println("RESPUESTA: " + sendMessage.getText());
			sendMessage(sendMessage);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getBotToken() {
		// SUSTITUIR POR EL TOKEN DE TU BOT
		return "NULL";
	}

	public void findCommand(String command) {
		// Comandos admitidos

		// Dado único
		if (command.matches("[d]\\d+")) {
			// d6
			// es una tirada de un solo dado
			simpleRoll(command);
		} else if (command.matches("[d]\\d+[!]")) {
			// d6!
			// es una tirada de un solo dado explosivo
		} else if (command.matches("[d]\\d+[+-]\\d+")) {
			// d5+6 d3-21
			// es una tirada de un dado con modificador
			simpleRollWithModifier(command);
		} else if (command.matches("[d]\\d+[!][+-]\\d+")) {
			// d5!+6 d3!+21
			// es una tirada de un dado explosivo con modificador
		}

		// Varios dados
		else if (command.matches("\\d+[d]\\d+")) {
			// 2d6
			// es una tirada de varios dados
			severalDice(command);
		} else if (command.matches("\\d+[d]\\d+[!]")) {
			// 2d6!
			// es una tirada de varios dados explosivos
		} else if (command.matches("\\d+[d]\\d+[+-]\\d+")) {
			// 3d5+6 10d3-21
			// es una tirada de varios dados con modificador
			severalDiceWithModifier(command);
		} else if (command.matches("\\d+[d]\\d+[!][+-]\\d+")) {
			// 3d5!+6 10d3!-21
			// es una tirada de varios dados explosivos con modificador
		}

		// Tirada múltiple de un solo dado
		else if (command.matches("\\d+[x][d]\\d+")) {
			// 3xd5
			// es una tirada multiple, de 1d5 cada una
		} else if (command.matches("\\d+[x][d]\\d+[!]")) {
			// 3xd5!
			// es una tirada multiple, de 1d5 explosivo cada una
		} else if (command.matches("\\d+[x][d]\\d+[+-]\\d+")) {
			// 3xd5+10
			// es una tirada multiple, de 1d5 cada una
			// donde aplicamos modificador
		} else if (command.matches("\\d+[x][d]\\d+[!][+-]\\d+")) {
			// 3xd5!+10
			// es una tirada multiple, de 1d5 explosivo cada una
			// donde aplicamos modificador
		}

		// Tirada múltiple de varios dados
		else if (command.matches("\\d+[x]\\d+[d]\\d+")) {
			// 6x4d6
			// es una tirada multiple, de 4d6 cada una
		} else if (command.matches("\\d+[x]\\d+[d]\\d+[!]")) {
			// 6x4d6!
			// es una tirada multiple, de 4d6 explosivos cada una
		} else if (command.matches("\\d+[x]\\d+[d]\\d+[+-]\\d+")) {
			// 3x2d5+10
			// es una tirada multiple, de 2d5 cada una
			// donde aplicamos modificador
		} else if (command.matches("\\d+[x]\\d+[d]\\d+[!][+-]\\d+")) {
			// 3x2d5!+10
			// es una tirada multiple, de 2d5 explosivos cada una
			// donde aplicamos modificador
		}

		// Tirada de varios dados, donde guardamos dados **nECESITA REPLANTEARSE**
		else if (command.matches("\\d+[d]\\d+[hlHL]\\d+")) {
			// 3d5h1
			// es una tirada, de 3d5 donde guardamos dados
		} else if (command.matches("\\d+[d]\\d+[hlHL]\\d+")) {
			// 3d5!h1
			// es una tirada, de 3d5 explosivos donde guardamos dados
		} else if (command.matches("\\d+[d]\\d+[hlHL]\\d+[+-]\\d+")) {
			// 3d5l1+5
			// es una tirada, de 3d5 donde guardamos dados y
			// aplicamos el modificador
		} else if (command.matches("\\d+[d]\\d+[hlHL]\\d+[+-]\\d+")) {
			// 3d5!l1+5
			// es una tirada, de 3d5 explosivos donde guardamos dados y
			// aplicamos el modificador
		}

		// Tirada multiple de varios dados donde guardamos dados **NECESITA
		// REPLANTEARSE**
		else if (command.matches("\\d+[x]\\d+[d]\\d+[hlHL]\\d+")) {
			// 6x4d6h3
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
		} else if (command.matches("\\d+[x]\\d+[d]\\d+[!][hlHL]\\d+")) {
			// 6x4d6!h3
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
		} else if (command.matches("\\d+[x]\\d+[d]\\d+[hlHL]\\d+[+-]\\d+")) {
			// 6x4d6l3+5
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
			// y aplicamos el modificador
		} else if (command.matches("\\d+[x]\\d+[d]\\d+[!][hlHL]\\d+[+-]\\d+")) {
			// 6x4d6!l3+5
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
			// y aplicamos el modificador
		}

		// Tirada de Fate
		else if (command.matches("\\b(fate|Fate|FATE)")) {
			// es una tirada de Fate
		} else if (command.matches("\\b(fate|Fate|FATE)[+-]\\d+")) {
			// esta es una tirada fate con modificador
		}

		// Tirada de Hitos
		else if (command.matches("\\b(hitos|Hitos|HITOS)")) {
			// esta es una tirada Hitos con modificador
		} else if (command.matches("\\b(hitos|Hitos|HITOS)[+-]\\d+")) {
			// esta es una tirada Hitos con modificador
		} else {
			sendMessage.setText("Comando no reconocido");
			answerUser();
		}
		/*
		 * TODO
		 * 
		 * higher y lower por encima de la cantidad de dados o 0 dados se cogen todos y
		 * ya 3d5l0 3d5h10 l y h pasan a DropLowest y KeepLowest (dl, kl) y DropHighest
		 * y Keephighest (dh, kh)
		 * 
		 * expresiones regulares por detectar
		 * 
		 * tiradas shadowrun
		 * 
		 * tiradas hackmaster/dados penetrantes (5d6!p) cualquier tirada que hagas, si
		 * sacas el máximo en el dado vuelves a tirar y sumas (con un -1 al nuevo dado)
		 * y si vuelves a sacar otra vez el máximo vuelves a tirar...
		 * 
		 * tiradas vampiro (dificultad 3d5>6)
		 * 
		 * reroll (relanzar) 3d6r<3
		 * 
		 * agrupar
		 * 
		 * texto (3d10 Tirada hitos) sladría algo tipo 4,4,6 Tirada hitos
		 * 
		 * https://wiki.roll20.net/Dice_Reference
		 * 
		 */
	}

	private void simpleRoll(String command) {
		numberOfSides = Integer.parseInt(command.substring(1));
		rollResult = 0;
		if (numberOfSides <= 1) {
			sendMessage.setText("¿Que dado conoces con una sola cara?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
			rollResult += valor;
			Collections.sort(dice);
			sendMessage.setText("[d" + numberOfSides + "]->" + dice + " = " + rollResult);
		}
		answerUser();
	}

	private void simpleRollWithModifier(String command) {
		// d5+5
		String stringAsset = command.substring(1);
		// 5+5
		String[] numericParts = null;
		char modifier = 0;
		if (stringAsset.contains("+")) {
			numericParts = stringAsset.split("\\+");
			modifier = '+';
		} else if (stringAsset.contains("-")) {
			numericParts = stringAsset.split("\\-");
			modifier = '-';
		}
		numberOfSides = Integer.parseInt(numericParts[0]);
		rollModifier = Integer.parseInt(numericParts[1]);
		rollResult = 0;

		if (numberOfSides <= 1) {
			sendMessage.setText("¿Que dado conoces con una sola cara?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
			rollResult += valor;
			Collections.sort(dice);
			if (modifier == '+') {
				sendMessage.setText("[d" + numberOfSides + "" + modifier + rollModifier + "]->" + dice + "+"
						+ rollModifier + " = " + (rollResult + rollModifier));
			} else if (modifier == '-') {
				sendMessage.setText("[d" + numberOfSides + "" + modifier + rollModifier + "]->" + dice + "-"
						+ rollModifier + " = " + (rollResult - rollModifier));
			}

			// sendMessage.setText("[d" + numberOfSides + "]->" + dice + " = " +
			// rollResult);
		}
		answerUser();
	}

	public void severalDice(String command) {
		String[] numericParts = command.split("d");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollResult = 0;

		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Máximo 50 dados");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Que dado conoces con una sola cara?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			}
			Collections.sort(dice);
			sendMessage.setText("[" + numberOfDice + "d" + numberOfSides + "]->" + dice + " = " + rollResult);
		}
		answerUser();
	}

	private void severalDiceWithModifier(String command) {
		String[] numericParts = command.split("d");
		numberOfDice = Integer.parseInt(numericParts[0]);
		String[] numericParts2 = null;
		char modifier = 0;
		if (numericParts[1].contains("+")) {
			numericParts2 = numericParts[1].split("\\+");
			modifier = '+';
		} else if (numericParts[1].contains("-")) {
			numericParts2 = numericParts[1].split("\\-");
			modifier = '-';
		} /*
			 * else if (numericParts[1].contains("*")) { numericParts2 =
			 * numericParts[1].split("\\*"); modifier = '*'; } else { numericParts2 =
			 * numericParts[1].split("\\/"); modifier = '/'; }
			 */
		numberOfSides = Integer.parseInt(numericParts2[0]);
		rollModifier = Integer.parseInt(numericParts2[1]);
		rollResult = 0;

		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Máximo 50 dados");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Que dado conoces con una sola cara?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			}
			Collections.sort(dice);
			if (modifier == '+') {
				sendMessage.setText("[" + numberOfDice + "d" + numberOfSides + "" + modifier + rollModifier + "]->"
						+ dice + "+" + rollModifier + " = " + (rollResult + rollModifier));
			} else if (modifier == '-') {
				sendMessage.setText("[" + numberOfDice + "d" + numberOfSides + "" + modifier + rollModifier + "]->"
						+ dice + "-" + rollModifier + " = " + (rollResult - rollModifier));
			}
		}
		answerUser();
	}

}
