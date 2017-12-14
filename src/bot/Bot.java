package bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.send.SendVideo;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {

	int numberOfDice = 0, numberOfSides = 0, rollModifier = 0, diceToKeep = 0, rollResult = 0;
	SendMessage sendMessage;
	SendVideo sendVideo;
	SendPhoto sendPhoto;
	SendDocument sendDocument;

	@Override
	public String getBotUsername() {
		// SUSTITUIR POR EL ALIAS DE TU BOT
		return "";
	}

	@Override
	public void onUpdateReceived(Update arg0) {

		// Almacenar la ID del chat para poder contestar
		// sendMessage = new SendMessage().setChatId(arg0.getMessage().getChatId());
		sendMessage = new SendMessage().setChatId(arg0.getMessage().getChatId());
		sendMessage.setReplyToMessageId(arg0.getMessage().getMessageId());
		sendVideo = new SendVideo().setChatId(arg0.getMessage().getChatId());
		sendPhoto = new SendPhoto().setChatId(arg0.getMessage().getChatId());
		// sendVideo.setReplyToMessageId(arg0.getMessage().getMessageId());

		// Imprimir por consola el mensaje recibido
		if (arg0.getMessage().hasText()) {
			System.out.println("\nMENSAJE RECIBIDO DE " + arg0.getMessage().getFrom().getFirstName() + ": "
					+ arg0.getMessage().getText().toLowerCase());

			// Si el mensaje es un comando, se intenta averiguar cual
			if (arg0.getMessage().getText().startsWith("/")) {
				findCommand(arg0.getMessage().getText().toLowerCase().substring(1));
			} else {
				// En caso contrario responde con un mensaje, ayudando a usar el bot
				sendMessage.setText("Hola " + arg0.getMessage().getFrom().getFirstName()
						+ " , Por ahora solo respondo a la tirada de dados, escribe /ayuda para más información");
			}
		}
		if (arg0.getMessage().hasDocument()) {
			System.out.println("Document: " + arg0.getMessage().getDocument());
		}
		if (arg0.getMessage().hasPhoto()) {
			List<PhotoSize> fotos = arg0.getMessage().getPhoto();
			for (PhotoSize foto : fotos) {
				System.out.println("Datos foto: " + foto);
			}
		}

	}

	public void answerUser() {
		// Respuesta del bot centralizada, todas las respuestas se mandan desde aquí
		// también se escribe un mensaje por consola
		try {
			System.out.println("RESPUESTA: " + sendMessage.getText());
			sendMessage.setParseMode("Markdown");
			execute(sendMessage);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getBotToken() {
		// SUSTITUIR POR EL TOKEN DE TU BOT
		return "";
	}

	private void findCommand(String command) {
		// Comando de pruebas
		if (command.equalsIgnoreCase("test")) {
			sendMessage.setText("Esto es un test");
			answerUser();
		}

		// Comandos admitidos
		else if (command.matches("(ayuda|help)")) {
			sendMessage.setText("Con este bot puedes lanzar una serie de dados ¡Y cada vez más!\n"
					+ "Simples [/d6]\nVarios dados [/3d6]\nCon modificador [/3d10+5 /2d12-7]\nDados explosivos [/1d20!+3]\nManteniendo dados [/3d20!h1 /6d10kh2 /4d8l1 /6d3!-8kl2]\nSavage Worlds [/s-2 /s4 /s6 /s8 /s10 /s12]\nIn Nomine Satanis [/ins]\nVampiro [/vampiro6 /vamp7d8 /v10d4]\nFate/Fudge [/f /fate+5 /fudge-7]");
			sendMessage.setParseMode("Markdown");
			answerUser();
		}
		// Dado único
		else if (command.matches("[d]\\d+")) {
			// d6
			// es una tirada de un solo dado
			simpleRoll(command);
		} else if (command.matches("[d]\\d+[!]")) {
			// d6!
			// es una tirada de un solo dado explosivo
			explosiveRoll(command);
		} else if (command.matches("[d]\\d+[+-]\\d+")) {
			// d5+6 d3-21
			// es una tirada de un dado con modificador
			simpleRollWithModifier(command);
		} else if (command.matches("[d]\\d+[!][+-]\\d+")) {
			// d5!+6 d3!+21
			// es una tirada de un dado explosivo con modificador
			simpleExplosiveRollWithModifier(command);
		}

		// Varios dados
		else if (command.matches("\\d+[d]\\d+")) {
			// 2d6
			// es una tirada de varios dados
			severalDice(command);
		} else if (command.matches("\\d+[d]\\d+[!]")) {
			// 2d6!
			// es una tirada de varios dados explosivos
			severalExplosiveDice(command);
		} else if (command.matches("\\d+[d]\\d+[+-]\\d+")) {
			// 3d5+6 10d3-21
			// es una tirada de varios dados con modificador
			severalDiceWithModifier(command);
		} else if (command.matches("\\d+[d]\\d+[!][+-]\\d+")) {
			// 3d5!+6 10d3!-21
			// es una tirada de varios dados explosivos con modificador
			severalExplosiveDiceWithModifier(command);
		}

		// Tirada de varios dados, donde guardamos los dados más altos
		else if (command.matches("\\d+[d]\\d+(h|kh|l|kl)\\d+")) {
			// 3d5h1 3d5kh1 3d5Kh1 3d5KH1 3d5kH1 3d5kH1
			// es una tirada, de 3d5 donde guardamos dados
			severalKeepRoll(command);
		} else if (command.matches("\\d+[d]\\d+[!](h|kh|l|kl)\\d+")) {
			// 3d5!h1
			// es una tirada, de 3d5 explosivos donde guardamos dados
			severalExplosiveKeepRoll(command);
		} else if (command.matches("\\d+[d]\\d+(h|kh|l|kl)\\d+[+-]\\d+")) {
			// 3d5l1+5
			// es una tirada, de 3d5 donde guardamos dados y
			// aplicamos el modificador
			severalKeepRollWithModifier(command);
		} else if (command.matches("\\d+[d]\\d+[!](h|kh|l|kl)\\d+[+-]\\d+")) {
			// 3d5!l1+5
			// es una tirada, de 3d5 explosivos donde guardamos dados y
			// aplicamos el modificador
			severalExplosiveKeepRollWithModifier(command);
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

		// Tirada multiple de varios dados donde guardamos dados **NECESITA
		// REPLANTEARSE**
		else if (command.matches("\\d+[x]\\d+[d]\\d+(h|kh|l|kl)\\d+")) {
			// 6x4d6h3
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
		} else if (command.matches("\\d+[x]\\d+[d]\\d+[!](h|kh|l|kl)\\d+")) {
			// 6x4d6!h3
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
		} else if (command.matches("\\d+[x]\\d+[d]\\d+(h|kh|l|kl)\\d+[+-]\\d+")) {
			// 6x4d6l3+5
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
			// y aplicamos el modificador
		} else if (command.matches("\\d+[x]\\d+[d]\\d+[!](h|kh|l|kl)\\d+[+-]\\d+")) {
			// 6x4d6!l3+5
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
			// y aplicamos el modificador
		}

		// Tirada de Fate
		else if (command.matches("(f|fate|fudge)")) {
			// es una tirada de Fate
			fateRoll(command);
		} else if (command.matches("(f|fate|fudge)[+-]\\d+")) {
			// esta es una tirada fate con modificador
			fateRollWithModifier(command);
		}

		// Tirada de Hitos
		else if (command.matches("(h|hitos)")) {
			// esta es una tirada Hitos con modificador
		} else if (command.matches("(h|hitos)[+-]\\d+")) {
			// esta es una tirada Hitos con modificador
		}

		// Tirada de Savage Worlds
		else if (command.matches("(s|savage)(-2|4|6|8|10|12)")) {
			// roll {1d6+1d8}kh1
			savageWorldRoll(command);
		}

		// Tirada de In Nomine Santis
		else if (command.matches("(ins)")) {
			// roll 3d6 sin ordenar
			inNomineSatanisRoll(command);
		}

		// Tirada de Vampiro
		else if (command.matches("(v|vampiro|vamp)\\d+")) {
			vampireRoll(command);
		}
		// Tirada de Vampirocon dificultad personalizada
		else if (command.matches("(v|vampiro|vamp)\\d+([d]\\d+)?")) {
			vampireRollWithCustomDificulty(command);
		}

		// Otros comandos
		else {
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
		// d5
		numberOfSides = Integer.parseInt(command.substring(1));
		rollResult = 0;
		if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
			rollResult += valor;
			Collections.sort(dice);
			sendMessage.setText("*[d" + numberOfSides + "]*-> [" + dice + "] = *" + rollResult + "*");
			sendMessage.setParseMode("Markdown");
		}
		answerUser();
	}

	private void explosiveRoll(String command) {
		// d5!
		String[] numericParts = command.split("\\D+");
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollResult = 0;
		if (numberOfSides <= 1) {
			sendMessage.setText("¿Un dado con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = 0;
			do {
				valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			} while (valor == numberOfSides);
			// Collections.sort(dice);
			sendMessage.setText("*[d" + numberOfSides + "!]*-> [" + dice + "] = *" + rollResult + "*");
			sendMessage.setParseMode("Markdown");
		}
		answerUser();
	}

	private void simpleRollWithModifier(String command) {
		// d5+5
		String[] numericParts = command.split("\\D+");
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		}
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollModifier = Integer.parseInt(numericParts[2]);
		rollResult = 0;
		if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
			rollResult += valor;
			Collections.sort(dice);
			if (modifier == '+') {
				sendMessage.setText("*[d" + numberOfSides + "" + modifier + rollModifier + "]*->[" + dice + "] + "
						+ rollModifier + " = *" + (rollResult + rollModifier) + "*");
			} else {
				sendMessage.setText("*[d" + numberOfSides + "" + modifier + rollModifier + "]*->[" + dice + "] - "
						+ rollModifier + " = *" + (rollResult - rollModifier) + "*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void simpleExplosiveRollWithModifier(String command) {
		// d5!+5
		String[] numericParts = command.split("\\D+");
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		}
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollModifier = Integer.parseInt(numericParts[2]);
		rollResult = 0;
		if (numberOfSides <= 1) {
			sendMessage.setText("¿Que dado conoces menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = 0;
			do {
				valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			} while (valor == numberOfSides);
			// Collections.sort(dice);
			if (modifier == '+') {
				sendMessage.setText("*[d" + numberOfSides + "!" + modifier + rollModifier + "]*->[" + dice + "] + "
						+ rollModifier + " = *" + (rollResult + rollModifier) + "*");
			} else {
				sendMessage.setText("*[d" + numberOfSides + "!" + modifier + rollModifier + "]*->[" + dice + "] - "
						+ rollModifier + " = *" + (rollResult - rollModifier) + "*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalDice(String command) {
		// 3d6
		String[] numericParts = command.split("d");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollResult = 0;

		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			}
			Collections.sort(dice);
			sendMessage
					.setText("*[" + numberOfDice + "d" + numberOfSides + "]*-> [" + dice + "] = *" + rollResult + "*");
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalExplosiveDice(String command) {
		// 3d6!
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollResult = 0;

		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = 0;
			for (int i = 0; i < numberOfDice; i++) {
				do {
					valor = (int) (Math.random() * numberOfSides + 1);
					dice.add(valor);
					rollResult += valor;
				} while (valor == numberOfSides);
			}
			// Collections.sort(dice);
			sendMessage
					.setText("*[" + numberOfDice + "d" + numberOfSides + "!]*-> [" + dice + "] = *" + rollResult + "*");
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalDiceWithModifier(String command) {
		// 3d6+10 2d8-7
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
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			}
			Collections.sort(dice);
			if (modifier == '+') {
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "" + modifier + rollModifier + "]*-> ["
						+ dice + "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "*");
			} else if (modifier == '-') {
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "" + modifier + rollModifier + "]*-> ["
						+ dice + "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalExplosiveDiceWithModifier(String command) {
		// 3d6!+5
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollModifier = Integer.parseInt(numericParts[2]);
		rollResult = 0;
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		} /*
			 * else if (numericParts[1].contains("*")) { numericParts2 =
			 * numericParts[1].split("\\*"); modifier = '*'; } else { numericParts2 =
			 * numericParts[1].split("\\/"); modifier = '/'; }
			 */
		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Que dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = 0;
			for (int i = 0; i < numberOfDice; i++) {
				do {
					valor = (int) (Math.random() * numberOfSides + 1);
					dice.add(valor);
					rollResult += valor;
				} while (valor == numberOfSides);
			}
			// Collections.sort(dice);
			if (modifier == '+') {
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!" + modifier + rollModifier + "]*-> ["
						+ dice + "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "*");
			} else if (modifier == '-') {
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!" + modifier + rollModifier + "]*-> ["
						+ dice + "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalKeepRoll(String command) {
		// 3d6h1
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		diceToKeep = Integer.parseInt(numericParts[2]);
		rollResult = 0;

		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			}
			Collections.sort(dice);
			if (diceToKeep > numberOfDice) {
				sendMessage.setText("La tirada no es de tantos dados");
			} else if (diceToKeep <= 0) {
				sendMessage.setText("No puedes quedarte con ningún dado");
			} else if (diceToKeep == numberOfDice) {
				sendMessage.setText(
						"*[" + numberOfDice + "d" + numberOfSides + "]*-> [" + dice + "] = *" + rollResult + "*");
			} else if (command.contains("h") || command.contains("hl")) {
				rollResult = 0;
				List<Integer> diceThrown = new ArrayList<Integer>(dice.subList(0, dice.size() - diceToKeep));
				List<Integer> diceKept = dice.subList(dice.size() - diceToKeep, dice.size());
				for (Integer value : diceKept) {
					rollResult += value;
				}
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "h" + diceToKeep + "]*->_" + diceThrown
						+ "_ [" + diceKept + "] = *" + rollResult + "*");
			} else {
				rollResult = 0;
				List<Integer> diceKept = dice.subList(0, diceToKeep);
				List<Integer> diceThrown = dice.subList(diceToKeep, dice.size());

				for (Integer value : diceKept) {
					rollResult += value;
				}
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "l" + diceToKeep + "]*->*" + diceKept
						+ "*_ " + diceThrown + "_ = *" + rollResult + "*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalExplosiveKeepRoll(String command) {
		// 3d6!h1
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		diceToKeep = Integer.parseInt(numericParts[2]);
		rollResult = 0;

		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = 0;
			for (int i = 0; i < numberOfDice; i++) {
				do {
					valor = (int) (Math.random() * numberOfSides + 1);
					dice.add(valor);
					rollResult += valor;
				} while (valor == numberOfSides);
			}
			Collections.sort(dice);
			if (diceToKeep > numberOfDice) {
				sendMessage.setText("La tirada no es de tantos dados");
			} else if (diceToKeep <= 0) {
				sendMessage.setText("No puedes quedarte con ningún dado");
			} else if (command.contains("h") || command.contains("hl")) {
				rollResult = 0;
				List<Integer> diceThrown = new ArrayList<Integer>(dice.subList(0, dice.size() - diceToKeep));
				List<Integer> diceKept = dice.subList(dice.size() - diceToKeep, dice.size());
				for (Integer value : diceKept) {
					rollResult += value;
				}
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!h" + diceToKeep + "]*->_" + diceThrown
						+ "_ *" + diceKept + "* = *" + rollResult + "*");
			} else {
				rollResult = 0;
				List<Integer> diceKept = dice.subList(0, diceToKeep);
				List<Integer> diceThrown = dice.subList(diceToKeep, dice.size());

				for (Integer value : diceKept) {
					rollResult += value;
				}
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!l" + diceToKeep + "]*->*" + diceKept
						+ "*_ " + diceThrown + "_ = *" + rollResult + "*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();

	}

	private void severalKeepRollWithModifier(String command) {
		// 10d6h3+5
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		diceToKeep = Integer.parseInt(numericParts[2]);
		rollModifier = Integer.parseInt(numericParts[3]);
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		}
		rollResult = 0;
		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			}
			Collections.sort(dice);
			if (diceToKeep > numberOfDice) {
				sendMessage.setText("La tirada no es de tantos dados");
			} else if (diceToKeep <= 0) {
				sendMessage.setText("No puedes quedarte con ningún dado");
			} else if (diceToKeep == numberOfDice) {
				if (modifier == '+') {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + rollModifier + "]*-> [" + dice
							+ "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "*");
				} else {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + rollModifier + "]*-> [" + dice
							+ "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "*");
				}
			} else if (command.contains("h") || command.contains("hl")) {
				rollResult = 0;
				List<Integer> diceThrown = new ArrayList<Integer>(dice.subList(0, dice.size() - diceToKeep));
				List<Integer> diceKept = dice.subList(dice.size() - diceToKeep, dice.size());
				for (Integer value : diceKept) {
					rollResult += value;
				}
				if (modifier == '+') {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "h" + diceToKeep + "+"
							+ rollModifier + "]*-> _" + diceThrown + "_ *" + diceKept + "* +" + rollModifier + " = *"
							+ (rollResult + rollModifier) + "*");
				} else {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "h" + diceToKeep + "-"
							+ rollModifier + "]*-> _" + diceThrown + "_ *" + diceKept + "* -" + rollModifier + " = *"
							+ (rollResult - rollModifier) + "*");
				}
			} else {
				rollResult = 0;
				List<Integer> diceKept = dice.subList(0, diceToKeep);
				List<Integer> diceThrown = dice.subList(diceToKeep, dice.size());

				for (Integer value : diceKept) {
					rollResult += value;
				}
				if (modifier == '+') {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "l" + diceToKeep + "+"
							+ rollModifier + "]*-> *" + diceKept + "* _" + diceThrown + "_ +" + rollModifier + " = *"
							+ (rollResult + rollModifier) + "*");
				} else {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "l" + diceToKeep + "-"
							+ rollModifier + "]*-> *" + diceKept + "* _" + diceThrown + "_ -" + rollModifier + " = *"
							+ (rollResult - rollModifier) + "*");
				}
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalExplosiveKeepRollWithModifier(String command) {
		// 10d6h3+5
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		diceToKeep = Integer.parseInt(numericParts[2]);
		rollModifier = Integer.parseInt(numericParts[3]);
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		}
		rollResult = 0;
		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = 0;
			for (int i = 0; i < numberOfDice; i++) {
				do {
					valor = (int) (Math.random() * numberOfSides + 1);
					dice.add(valor);
					rollResult += valor;
				} while (valor == numberOfSides);
			}
			Collections.sort(dice);
			if (diceToKeep > numberOfDice) {
				sendMessage.setText("La tirada no es de tantos dados");
			} else if (diceToKeep <= 0) {
				sendMessage.setText("No puedes quedarte con ningún dado");
			} else if (diceToKeep == numberOfDice) {
				if (modifier == '+') {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + rollModifier + "]*-> [" + dice
							+ "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "*");
				} else {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + rollModifier + "]*-> [" + dice
							+ "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "*");
				}
			} else if (command.contains("h") || command.contains("hl")) {
				rollResult = 0;
				List<Integer> diceThrown = new ArrayList<Integer>(dice.subList(0, dice.size() - diceToKeep));
				List<Integer> diceKept = dice.subList(dice.size() - diceToKeep, dice.size());
				for (Integer value : diceKept) {
					rollResult += value;
				}
				if (modifier == '+') {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!h" + diceToKeep + "+"
							+ rollModifier + "]*-> _" + diceThrown + "_ *" + diceKept + "* +" + rollModifier + " = *"
							+ (rollResult + rollModifier) + "*");
				} else {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!h" + diceToKeep + "-"
							+ rollModifier + "]*-> _" + diceThrown + "_ *" + diceKept + "* -" + rollModifier + " = *"
							+ (rollResult - rollModifier) + "*");
				}
			} else {
				rollResult = 0;
				List<Integer> diceKept = dice.subList(0, diceToKeep);
				List<Integer> diceThrown = dice.subList(diceToKeep, dice.size());

				for (Integer value : diceKept) {
					rollResult += value;
				}
				if (modifier == '+') {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!l" + diceToKeep + "+"
							+ rollModifier + "]*-> *" + diceKept + "* _" + diceThrown + "_ +" + rollModifier + " = *"
							+ (rollResult + rollModifier) + "*");
				} else {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!l" + diceToKeep + "-"
							+ rollModifier + "]*-> *" + diceKept + "* _" + diceThrown + "_ -" + rollModifier + " = *"
							+ (rollResult - rollModifier) + "*");
				}
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void savageWorldRoll(String command) {
		// "(s|savage)(4|6|8|10|12)"
		// roll {1d6+1d8}kh1
		String[] numericParts = command.split("\\D+");
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollResult = 0;

		ArrayList<Integer> wildDice = new ArrayList<Integer>();
		int wild = 0, wildResult = 0;
		do {
			wild = (int) (Math.random() * 6 + 1);
			wildDice.add(wild);
			wildResult += wild;
			System.out.println("wild" + wild);
		} while (wild == 6);

		ArrayList<Integer> dice = new ArrayList<Integer>();
		int value = 0;
		do {
			value = (int) (Math.random() * numberOfSides + 1);
			dice.add(value);
			rollResult += value;
			System.out.println("roll" + value);
		} while (value == numberOfSides);
		int finalResult = (wildResult > rollResult) ? wildResult : rollResult;
		int increases = 1;
		while ((finalResult - increases * 4) - 4 >= 0) {
			increases++;
		}
		if (command.contains("-")) {
			sendMessage.setText("*Savage Worlds [6] [4] -2*\nSalvaje: [" + wildDice + "] -2 = " + (wildResult - 2)
					+ "\nHabilidad: [" + dice + "] -2 = " + (rollResult - 2) + "\n*Total: " + (finalResult - 2) + " -> "
					+ (increases - 1) + " aumento/s*");
		} else {
			sendMessage.setText("*Savage Worlds [6] [" + numberOfSides + "]*\nSalvaje: [" + wildDice + "] = "
					+ wildResult + "\nHabilidad: [" + dice + "] = " + rollResult + "\n*Total: " + finalResult + " -> "
					+ (increases - 1) + " aumento/s*");
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void inNomineSatanisRoll(String command) {
		// 3d6
		rollResult = 0;
		numberOfDice = 3;
		numberOfSides = 6;
		String roll = "";
		for (int i = 0; i < numberOfDice; i++) {
			int valor = (int) (Math.random() * numberOfSides + 1);
			roll += valor;
		}
		if (roll == "111") {
			sendMessage.setText("*[In Nomine Satanis]* = *" + roll + "*");
			sendVideo.setVideo("https://media.giphy.com/media/l2QE7PACf4cJccwA8/giphy.gif");
			try {
				sendVideo(sendVideo);
			} catch (TelegramApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (roll == "666") {
			sendMessage.setText("*[In Nomine Satanis]* = *" + roll + "*");
			sendVideo.setVideo("https://media.giphy.com/media/hB5vNhUepvcek/giphy.mp4");
			try {
				sendVideo(sendVideo);
			} catch (TelegramApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			sendMessage.setText("*[In Nomine Satanis]* = " + roll);
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
		sendPhoto.setPhoto("https://i.imgur.com/PWsDzmW.jpg");
		try {
			sendPhoto(sendPhoto);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void vampireRoll(String command) {
		// v7
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[1]);
		int difficulty = 6;
		numberOfSides = 10;
		int success = 0;
		int ones = 0;

		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				if (valor == 1) {
					ones++;
				}
				if (valor >= difficulty) {
					success++;
				}
			}
			Collections.sort(dice);
			if (success > ones) {
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + "]*-> [" + dice
						+ "] = *" + (success - ones) + " éxito/s*");
			} else if (success == ones) {
				sendMessage.setText(
						"*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + "]*-> [" + dice + "] = *Fallo*");
			} else {
				sendMessage.setText(
						"*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + "]*-> [" + dice + "] = *Pífia*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void vampireRollWithCustomDificulty(String command) {
		// v6d3
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[1]);
		int difficulty = 6;
		if (difficulty == Integer.parseInt(numericParts[2]))
			;
		{
			difficulty = Integer.parseInt(numericParts[2]);
		}
		System.out.println("entrada: " + command);
		System.out.println("numericParts: " + numericParts[2]);
		System.out.println("dados: " + numberOfDice + " dificultad: " + difficulty);
		numberOfSides = 10;
		int success = 0;
		int ones = 0;

		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				if (valor == 1) {
					ones++;
				}
				if (valor >= difficulty) {
					success++;
				}
			}
			Collections.sort(dice);
			if (success > ones) {
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + "]*-> [" + dice
						+ "] = *" + (success - ones) + " éxito/s*");
			} else if (success == ones) {
				sendMessage.setText(
						"*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + "]*-> [" + dice + "] = *Fallo*");
			} else {
				sendMessage.setText(
						"*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + "]*-> [" + dice + "] = *Pífia*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void fateRoll(String command) {
		numberOfDice = 4;
		numberOfSides = 6;
		rollResult = 0;
		ArrayList<Integer> dice = new ArrayList<Integer>();
		for (int i = 0; i < numberOfDice; i++) {
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
		}
		sendMessage.setText("*[FATE]*-> ");
		for (Integer dado : dice) {
			if (dado == 1 || dado == 2) {
				sendMessage.setText(sendMessage.getText() + "➖");
				rollResult -= 1;
			} else if (dado == 3 || dado == 4) {
				sendMessage.setText(sendMessage.getText() + "⭕");
			} else if (dado == 5 || dado == 6) {
				sendMessage.setText(sendMessage.getText() + "➕");
				rollResult += 1;
			}
		}
		sendMessage.setText(sendMessage.getText() + " = *" + rollResult + "*");
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void fateRollWithModifier(String command) {
		// fate+5 f-7
		String[] numericParts = command.split("\\D+");
		rollModifier = Integer.parseInt(numericParts[1]);
		numberOfDice = 4;
		numberOfSides = 6;
		rollResult = 0;
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		}
		ArrayList<Integer> dice = new ArrayList<Integer>();
		for (int i = 0; i < numberOfDice; i++) {
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
		}
		sendMessage.setText("*[FATE]*-> ");
		for (Integer dado : dice) {
			if (dado == 1 || dado == 2) {
				sendMessage.setText(sendMessage.getText() + "➖");
				rollResult -= 1;
			} else if (dado == 3 || dado == 4) {
				sendMessage.setText(sendMessage.getText() + "⭕");
			} else if (dado == 5 || dado == 6) {
				sendMessage.setText(sendMessage.getText() + "➕");
				rollResult += 1;
			}
		}
		if (modifier == '+') {
			sendMessage
					.setText(sendMessage.getText() + "+" + rollModifier + " = *" + (rollResult + rollModifier) + "*");
		} else {
			sendMessage
					.setText(sendMessage.getText() + "-" + rollModifier + " = *" + (rollResult - rollModifier) + "*");
		}
		// sendMessage.setText(sendMessage.getText() + " *" + rollResult + "*");
		sendMessage.setParseMode("Markdown");
		answerUser();
	}
}
