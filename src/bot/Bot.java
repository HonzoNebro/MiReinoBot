package bot;

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
		System.out.println(arg0.getMessage().getFrom().getFirstName() + ": " + arg0.getMessage().getText());
		
		//respuesta de prueba
		SendMessage sendMessage = new SendMessage().setChatId(arg0.getMessage().getChatId());
		sendMessage.setText("Hola "+arg0.getMessage().getFrom().getFirstName() + " , ¿Quieres jugar a rol? Pues paciencia, que solo se decir esta frase");
		
		try {
			sendMessage(sendMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getBotToken() {
		// TODO Auto-generated method stub
		return "504332506:AAGcJ4mgbA9B-VqdW0fXwPm3iB41MZ0z3hY";
	}

}
