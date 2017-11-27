package bot;

import java.util.ArrayList;
import java.util.Collections;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

public class Bot extends TelegramLongPollingBot {

	@Override
	public String getBotUsername() {
		return null;
	}

	@Override
	public void onUpdateReceived(Update arg0) {

		// almacenar ID del remitente
		SendMessage sendMessage = new SendMessage().setChatId(arg0.getMessage().getChatId());

		// imprimir por consola el mensaje recibido
		System.out.println("\n"+arg0.getMessage().getFrom().getFirstName() + ": " + arg0.getMessage().getText());

		// si el mensaje es un comando asume que va a ser una tirada con formato: /1d20
		if (arg0.getMessage().getText().startsWith("/")) {
			String mensaje = arg0.getMessage().getText().substring(1);
			//prueba de expresión regular
			/*if(mensaje.matches("\\d++[d]\\d++([+-]\\d++)*")) {
				System.out.println("es una tirada!!");
			}*/
			
			int numDados = 0;
			int numCaras = 2;
			int valorBonificador = 0;
			int sumaTotal = 0;
			// separa las partes numéricas
			try {
				String[] partesNumericas = arg0.getMessage().getText().split("d");
				numDados = Integer.parseInt(partesNumericas[0].substring(1));
				numCaras = Integer.parseInt(partesNumericas[1]);
				
				if(mensaje.contains("+")){
					valorBonificador = Integer.parseInt(mensaje.substring(mensaje.indexOf("+")));
					System.out.println("bonificador: "+valorBonificador);
				}else if (mensaje.contains("-") ){
					valorBonificador = Integer.parseInt(mensaje.substring(mensaje.indexOf("-")));
					System.out.println("bonificador: "+valorBonificador);
				}
				System.out.println("Sin bonificador que mostrar");
				
				if ((numDados <= 0) || (numDados > 50)) {
					sendMessage.setText("No puedo tirar esa cantidad de dados. Máximo 50 dados");
				} else if (numCaras <= 1) {
					sendMessage.setText("¿Que dado conoces con una sola cara?");
				} else {
					//System.out.println(numDados + " dado/s de: " + numCaras + " caras");
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
			//System.out.println("Respuesta del bot: " + sendMessage.getText());
			sendMessage(sendMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getBotToken() {
		// TODO Auto-generated method stub
		return "AQUI VA UN TOKEN QUE HAYAS CREADO";
	}

}
