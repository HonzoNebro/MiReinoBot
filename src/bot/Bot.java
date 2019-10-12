package bot;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bot extends TelegramLongPollingBot {

    private static final String MARKDOWN = "Markdown";
    private static final String ERROR_CARAS = "¿Qué dado conoces con menos de dos caras?";
    private static final String ERROR_DEMASIADOS_DADOS = "No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50";
    private static final String ERROR_RECUPERAR_DEMSIADOS_DADOS = "La tirada no es de tantos dados";
    private static final String ERROR_RECUPERAR_CERO_DADOS = "No puedes cero dados";
    private static final String RESULTADO_PIFIA = "] = *💀Pífia💀*";
    private static final String JUEGO_HITOS = "*[Hitos]*➡ [";
    private static final String RESULTADO_PIFIA_TRIPLE = "] ➡ * 💀Pífia Triple💀*";
    private static final String RESULTADO_PIFIA_DOBLE = "] ➡ * 💀Pífia Doble💀*";
    private static final String TOTAL = "\n*Total: ";
    private static final String AUMENTOS = " aumento/s*";
    private static final String RESULTADO_FALLO = "💀FALLO💀";
    private static final String RESULTADO_EXITO_PARCIAL = "ÉXITO PARCIAL";
    private static final String RESULTADO_EXITO = "ÉXITO";
    private static final String JUEGO_PBTA = "*[PbtA]* *[";
    private static final String JUEGO_ERIANDOS = "*[Eriandos]* *[";
    private static final String FLECHA_DERECHA = "]*➡ [";
    private static final String IGUAL = "] = *";
    private static final String RESULTADO_MENOS_DOS = "] -2 = ";
    private static final String HABILIDAD = "\nHabilidad: [";
    private static final String ERROR_COMANDO_NO_RECONOCIDO = "Comando no reconocido. Escribe /ayuda para más información";
    private Logger logger = Logger.getLogger(Bot.class.getName());
    private int numberOfDice = 0;
    private int numberOfSides = 0;
    private int rollModifier = 0;
    private int diceToKeep = 0;
    private int rollResult = 0;
    private SendMessage sendMessage;
    private SendVideo sendVideo;
    private SendPhoto sendPhoto;

    @Override
    public String getBotUsername() {
        // SUSTITUIR POR EL ALIAS DE TU BOT
        return "joderconelbot";
    }

    @Override
    public String getBotToken() {
        // SUSTITUIR POR EL TOKEN DE TU BOT
        return "491553940:AAGvx96QJxGm_xffRVlBWTBRj9tuU4oAEYM";
    }

    @Override
    public void onUpdateReceived(Update arg0) {

        // Almacenar la ID del chat para poder contestar
        sendMessage = new SendMessage().setChatId(arg0.getMessage().getChatId());
        sendMessage.setReplyToMessageId(arg0.getMessage().getMessageId());
        sendVideo = new SendVideo().setChatId(arg0.getMessage().getChatId());
        sendPhoto = new SendPhoto().setChatId(arg0.getMessage().getChatId());

        // Si el mensaje es un comando, se intenta averiguar cual
        if (arg0.getMessage().getText().startsWith("/")) {
            findCommand(arg0.getMessage().getText().toLowerCase().substring(1));
        } else {
            // En caso contrario responde con un mensaje, ayudando a usar el bot
            sendMessage.setText("Hola " + arg0.getMessage().getFrom().getFirstName()
                    + " , Por ahora solo respondo a la tirada de dados, escribe /ayuda para más información");
        }

    }

    private void answerUser() {
        // Respuesta del bot centralizada, todas las respuestas se mandan desde aquí
        // también se escribe un mensaje por consola
        try {
            System.out.println("RESPUESTA: 🎲" + sendMessage.getText());
            sendMessage.setText(EmojiParser.parseToUnicode("🎲" + sendMessage.getText()));
            sendMessage.setParseMode(MARKDOWN);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.log(Level.WARNING, e.getMessage());
        }

    }

    private void findCommand(String command) {
        // Comandos admitidos
        if (command.matches("(ayuda|help|start|test)")) {
            sendMessage.setText("Con este bot puedes lanzar estos dados para jugar a rol ¡Más próximamente!\n"
                    + "Simples [/d6]\nVarios dados [/3d6]\nCon modificador [/3d10+5 /2d12-7]\nDados explosivos [/1d20!+3]\nManteniendo dados [/3d20!h1 /6d10kh2 /4d8l1]\nSavage Worlds [/s-2 /s4 /s6 /s8 /s10 /s12]\nIn Nomine Satanis [/ins]\nVampiro [/vampiro6 /vamp7d8 /v10d4]\nFate/Fudge [/f /fate+5 /fudge-7]\nHitos [/h /h+7 /hitos /hitos+5]\nPbtA [/pbta /pbta+1 /p-2 /p+1]\nEste bot es de código abierto -> https://github.com/HonzoNebro/MiReinoBot");
            sendMessage.setParseMode(MARKDOWN);
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

        // Target 1d20>10 3d6>5
        else if (command.matches("\\d+[d]\\d+[>]\\d+")) {
            rollOver(command);
        } else if (command.matches("\\d+[d]\\d+[<]\\d+")) {
            rollUnder(command);
        }

        // Target 1d20<10 3d6<5

        // Tirada de Fate
        else if (command.matches(
                "(f|fate|fudge|f@mireinoporunmasterbot|fate@mireinoporunmasterbot|fudge@mireinoporunmasterbot)")) {
            // es una tirada de Fate
            fateRoll();
        } else if (command.matches("(f|fate|fudge)[+-]\\d+")) {
            // esta es una tirada fate con modificador
            fateRollWithModifier(command);
        }

        // Tirada de Hitos
        else if (command.matches("(h|hitos|h@mireinoporunmasterbot|hitos@mireinoporunmasterbot)")) {
            hitosRoll();
            // esta es una tirada Hitos con modificador
        } else if (command.matches("(h|hitos)[+]\\d+")) {
            // esta es una tirada Hitos con modificador
            hitosRollWithModifier(command);
        }

        // Tirada de Savage Worlds
        else if (command.matches("(s|savage)(4|6|8|10|12)")) {
            // roll {1d6+1d8}kh1
            savageWorldRoll(command);
        } else if (command.matches("(s|savage)(-2)")) {
            // roll {1d6-2, 1d4-2}kh1
            negativeSavageWorldRoll();
        }

        // Tirada de In Nomine Santis
        else if (command.matches("(ins|ins@mireinoporunmasterbot)")) {
            // roll 3d6 sin ordenar
            inNomineSatanisRoll();
        }

        // Tirada de Vampiro
        else if (command.matches("(v|vampiro|vamp)\\d+")) {
            vampireRoll(command);
        }
        // Tirada de Vampiro con dificultad personalizada
        else if (command.matches("(v|vampiro|vamp)\\d+([d]\\d+)?")) {
            vampireRollWithCustomDificulty(command);
        }

        // Tirada de Powered by the Apocalypse
        else if (command.matches("(pbta|p|p@mireinoporunmasterbot|pbta@mireinoporunmasterbot)")) {
            // pbta p
            poweredByTheApocapilse();
        }
        // Tirada de Powered by the Apocalypse
        else if (command.matches("(pbta|p)[+-]\\d+")) {
            // pbta+1 p-3
            poweredByTheApocapilseWithModifier(command);
        }

        // Tiradas de Cronicas de Ériandos
        else if (command.matches("(e|eriandos)")) {
            tiradaEriandos(command);
        } else if (command.matches("(e|eriandos)[+-]\\d+")) {
            tiradaEriandos(command);
        }

        // Otros comandos
        else {
            sendMessage.setText(ERROR_COMANDO_NO_RECONOCIDO);
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
            sendMessage.setText(ERROR_CARAS);
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
            int valor = (int) (Math.random() * numberOfSides + 1);
            dice.add(valor);
            rollResult += valor;
            Collections.sort(dice);
            sendMessage.setText("*[d" + numberOfSides + FLECHA_DERECHA + dice + IGUAL + rollResult + "*");
            sendMessage.setParseMode(MARKDOWN);
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
            ArrayList<Integer> dice = new ArrayList<>();
            int valor = 0;
            do {
                valor = (int) (Math.random() * numberOfSides + 1);
                dice.add(valor);
                rollResult += valor;
            } while (valor == numberOfSides);
            sendMessage.setText("*[d" + numberOfSides + "!]*-> [" + dice + IGUAL + rollResult + "*");
            sendMessage.setParseMode(MARKDOWN);
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
            sendMessage.setText(ERROR_CARAS);
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
            int valor = (int) (Math.random() * numberOfSides + 1);
            dice.add(valor);
            rollResult += valor;
            Collections.sort(dice);
            if (modifier == '+') {
                sendMessage.setText("*[d" + numberOfSides + "" + modifier + rollModifier + FLECHA_DERECHA + dice + "] + "
                        + rollModifier + " = *" + (rollResult + rollModifier) + "*");
            } else {
                sendMessage.setText("*[d" + numberOfSides + "" + modifier + rollModifier + FLECHA_DERECHA + dice + "] - "
                        + rollModifier + " = *" + (rollResult - rollModifier) + "*");
            }
        }
        sendMessage.setParseMode(MARKDOWN);
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
            ArrayList<Integer> dice = new ArrayList<>();
            int valor = 0;
            do {
                valor = (int) (Math.random() * numberOfSides + 1);
                dice.add(valor);
                rollResult += valor;
            } while (valor == numberOfSides);
            if (modifier == '+') {
                sendMessage.setText("*[d" + numberOfSides + "!" + modifier + rollModifier + FLECHA_DERECHA + dice + "] + "
                        + rollModifier + " = *" + (rollResult + rollModifier) + "*");
            } else {
                sendMessage.setText("*[d" + numberOfSides + "!" + modifier + rollModifier + FLECHA_DERECHA + dice + "] - "
                        + rollModifier + " = *" + (rollResult - rollModifier) + "*");
            }
        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void severalDice(String command) {
        // 3d6
        String[] numericParts = command.split("d");
        numberOfDice = Integer.parseInt(numericParts[0]);
        numberOfSides = Integer.parseInt(numericParts[1]);
        rollResult = 0;

        if ((numberOfDice <= 0) || (numberOfDice > 50)) {
            sendMessage.setText(ERROR_DEMASIADOS_DADOS);
        } else if (numberOfSides <= 1) {
            sendMessage.setText(ERROR_CARAS);
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
            for (int i = 0; i < numberOfDice; i++) {
                int valor = (int) (Math.random() * numberOfSides + 1);
                dice.add(valor);
                rollResult += valor;
            }
            Collections.sort(dice);
            sendMessage
                    .setText("*[" + numberOfDice + "d" + numberOfSides + FLECHA_DERECHA + dice + IGUAL + rollResult + "*");
        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void severalExplosiveDice(String command) {
        // 3d6!
        String[] numericParts = command.split("\\D+");
        numberOfDice = Integer.parseInt(numericParts[0]);
        numberOfSides = Integer.parseInt(numericParts[1]);
        rollResult = 0;

        if ((numberOfDice <= 0) || (numberOfDice > 50)) {
            sendMessage.setText(ERROR_DEMASIADOS_DADOS);
        } else if (numberOfSides <= 1) {
            sendMessage.setText(ERROR_CARAS);
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
            int valor = 0;
            for (int i = 0; i < numberOfDice; i++) {
                do {
                    valor = (int) (Math.random() * numberOfSides + 1);
                    dice.add(valor);
                    rollResult += valor;
                } while (valor == numberOfSides);
            }
            sendMessage
                    .setText("*[" + numberOfDice + "d" + numberOfSides + "!]*-> [" + dice + IGUAL + rollResult + "*");
        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void severalDiceWithModifier(String command) {
        // 3d6+10 2d8-7
        String[] numericParts = command.split("\\D+");
        numberOfDice = Integer.parseInt(numericParts[0]);
        numberOfSides = Integer.parseInt(numericParts[1]);
        rollModifier = Integer.parseInt(numericParts[2]);
        rollResult = 0;
        char modifier = '+';
        if (command.contains("-")) {
            modifier = '-';
        }
        if ((numberOfDice <= 0) || (numberOfDice > 50)) {
            sendMessage.setText(ERROR_DEMASIADOS_DADOS);
        } else if (numberOfSides <= 1) {
            sendMessage.setText(ERROR_CARAS);
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
            for (int i = 0; i < numberOfDice; i++) {
                int valor = (int) (Math.random() * numberOfSides + 1);
                dice.add(valor);
                rollResult += valor;
            }
            Collections.sort(dice);
            if (modifier == '+') {
                sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "" + modifier + rollModifier + FLECHA_DERECHA
                        + dice + "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "*");
            } else if (modifier == '-') {
                sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "" + modifier + rollModifier + FLECHA_DERECHA
                        + dice + "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "*");
            }
        }
        sendMessage.setParseMode(MARKDOWN);
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
        }
        if ((numberOfDice <= 0) || (numberOfDice > 50)) {
            sendMessage.setText(ERROR_DEMASIADOS_DADOS);
        } else if (numberOfSides <= 1) {
            sendMessage.setText("¿Que dado conoces con menos de dos caras?");
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
            int valor = 0;
            for (int i = 0; i < numberOfDice; i++) {
                do {
                    valor = (int) (Math.random() * numberOfSides + 1);
                    dice.add(valor);
                    rollResult += valor;
                } while (valor == numberOfSides);
            }
            if (modifier == '+') {
                sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!" + modifier + rollModifier + FLECHA_DERECHA
                        + dice + "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "*");
            } else if (modifier == '-') {
                sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!" + modifier + rollModifier + FLECHA_DERECHA
                        + dice + "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "*");
            }
        }
        sendMessage.setParseMode(MARKDOWN);
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
            sendMessage.setText(ERROR_DEMASIADOS_DADOS);
        } else if (numberOfSides <= 1) {
            sendMessage.setText(ERROR_CARAS);
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
            for (int i = 0; i < numberOfDice; i++) {
                int valor = (int) (Math.random() * numberOfSides + 1);
                dice.add(valor);
                rollResult += valor;
            }
            Collections.sort(dice);
            if (diceToKeep > numberOfDice) {
                sendMessage.setText(ERROR_RECUPERAR_DEMSIADOS_DADOS);
            } else if (diceToKeep <= 0) {
                sendMessage.setText(ERROR_RECUPERAR_CERO_DADOS);
            } else if (diceToKeep == numberOfDice) {
                sendMessage.setText(
                        "*[" + numberOfDice + "d" + numberOfSides + FLECHA_DERECHA + dice + IGUAL + rollResult + "*");
            } else if (command.contains("h") || command.contains("hl")) {
                rollResult = 0;
                List<Integer> diceThrown = new ArrayList<>(dice.subList(0, dice.size() - diceToKeep));
                List<Integer> diceKept = dice.subList(dice.size() - diceToKeep, dice.size());
                for (Integer value : diceKept) {
                    rollResult += value;
                }
                sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "h" + diceToKeep + "]*->_" + diceThrown
                        + "_ [" + diceKept + IGUAL + rollResult + "*");
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
        sendMessage.setParseMode(MARKDOWN);
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
            sendMessage.setText(ERROR_DEMASIADOS_DADOS);
        } else if (numberOfSides <= 1) {
            sendMessage.setText(ERROR_CARAS);
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
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
                sendMessage.setText(ERROR_RECUPERAR_DEMSIADOS_DADOS);
            } else if (diceToKeep <= 0) {
                sendMessage.setText(ERROR_RECUPERAR_CERO_DADOS);
            } else if (command.contains("h") || command.contains("hl")) {
                rollResult = 0;
                List<Integer> diceThrown = new ArrayList<>(dice.subList(0, dice.size() - diceToKeep));
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
        sendMessage.setParseMode(MARKDOWN);
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
            sendMessage.setText(ERROR_DEMASIADOS_DADOS);
        } else if (numberOfSides <= 1) {
            sendMessage.setText(ERROR_CARAS);
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
            for (int i = 0; i < numberOfDice; i++) {
                int valor = (int) (Math.random() * numberOfSides + 1);
                dice.add(valor);
                rollResult += valor;
            }
            Collections.sort(dice);
            if (diceToKeep > numberOfDice) {
                sendMessage.setText(ERROR_RECUPERAR_DEMSIADOS_DADOS);
            } else if (diceToKeep <= 0) {
                sendMessage.setText(ERROR_RECUPERAR_CERO_DADOS);
            } else if (diceToKeep == numberOfDice) {
                if (modifier == '+') {
                    sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + rollModifier + FLECHA_DERECHA + dice
                            + "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "*");
                } else {
                    sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + rollModifier + FLECHA_DERECHA + dice
                            + "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "*");
                }
            } else if (command.contains("h") || command.contains("hl")) {
                rollResult = 0;
                List<Integer> diceThrown = new ArrayList<>(dice.subList(0, dice.size() - diceToKeep));
                List<Integer> diceKept = dice.subList(dice.size() - diceToKeep, dice.size());
                for (Integer value : diceKept) {
                    rollResult += value;
                }
                if (modifier == '+') {
                    sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "h" + diceToKeep + "+"
                            + rollModifier + FLECHA_DERECHA + diceThrown + "_ *" + diceKept + "* +" + rollModifier + " = *"
                            + (rollResult + rollModifier) + "*");
                } else {
                    sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "h" + diceToKeep + "-"
                            + rollModifier + FLECHA_DERECHA + diceThrown + "_ *" + diceKept + "* -" + rollModifier + " = *"
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
                            + rollModifier + FLECHA_DERECHA + diceKept + "* _" + diceThrown + "_ +" + rollModifier + " = *"
                            + (rollResult + rollModifier) + "*");
                } else {
                    sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "l" + diceToKeep + "-"
                            + rollModifier + FLECHA_DERECHA + diceKept + "* _" + diceThrown + "_ -" + rollModifier + " = *"
                            + (rollResult - rollModifier) + "*");
                }
            }
        }
        sendMessage.setParseMode(MARKDOWN);
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
            sendMessage.setText(ERROR_DEMASIADOS_DADOS);
        } else if (numberOfSides <= 1) {
            sendMessage.setText(ERROR_CARAS);
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
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
                sendMessage.setText(ERROR_RECUPERAR_DEMSIADOS_DADOS);
            } else if (diceToKeep <= 0) {
                sendMessage.setText(ERROR_RECUPERAR_CERO_DADOS);
            } else if (diceToKeep == numberOfDice) {
                if (modifier == '+') {
                    sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + rollModifier + FLECHA_DERECHA + dice
                            + "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "*");
                } else {
                    sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + rollModifier + FLECHA_DERECHA + dice
                            + "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "*");
                }
            } else if (command.contains("h") || command.contains("hl")) {
                rollResult = 0;
                List<Integer> diceThrown = new ArrayList<>(dice.subList(0, dice.size() - diceToKeep));
                List<Integer> diceKept = dice.subList(dice.size() - diceToKeep, dice.size());
                for (Integer value : diceKept) {
                    rollResult += value;
                }
                if (modifier == '+') {
                    sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!h" + diceToKeep + "+"
                            + rollModifier + FLECHA_DERECHA + diceThrown + "_ *" + diceKept + "* +" + rollModifier + " = *"
                            + (rollResult + rollModifier) + "*");
                } else {
                    sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!h" + diceToKeep + "-"
                            + rollModifier + FLECHA_DERECHA + diceThrown + "_ *" + diceKept + "* -" + rollModifier + " = *"
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
                            + rollModifier + FLECHA_DERECHA + diceKept + "* _" + diceThrown + "_ +" + rollModifier + " = *"
                            + (rollResult + rollModifier) + "*");
                } else {
                    sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!l" + diceToKeep + "-"
                            + rollModifier + FLECHA_DERECHA + diceKept + "* _" + diceThrown + "_ -" + rollModifier + " = *"
                            + (rollResult - rollModifier) + "*");
                }
            }
        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void rollOver(String command) {
        // 1d20>10 3d6>5
        String[] numericParts = command.split("\\D+");
        numberOfDice = Integer.parseInt(numericParts[0]);
        numberOfSides = Integer.parseInt(numericParts[1]);
        rollResult = 0;
        int target = Integer.parseInt(numericParts[2]);

        if ((numberOfDice <= 0) || (numberOfDice > 50)) {
            sendMessage.setText(ERROR_DEMASIADOS_DADOS);
        } else if (numberOfSides <= 1) {
            sendMessage.setText(ERROR_CARAS);
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
            int valor = 0;
            for (int i = 0; i < numberOfDice; i++) {
                do {
                    valor = (int) (Math.random() * numberOfSides + 1);
                    dice.add(valor);
                    rollResult += valor;
                } while (valor > target);
            }
            sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + ">" + target + FLECHA_DERECHA + dice + IGUAL
                    + rollResult + "*");
        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void rollUnder(String command) {
        // 1d20<10 3d6<5
        String[] numericParts = command.split("\\D+");
        numberOfDice = Integer.parseInt(numericParts[0]);
        numberOfSides = Integer.parseInt(numericParts[1]);
        rollResult = 0;
        int target = Integer.parseInt(numericParts[2]);

        if ((numberOfDice <= 0) || (numberOfDice > 50)) {
            sendMessage.setText(ERROR_DEMASIADOS_DADOS);
        } else if (numberOfSides <= 1) {
            sendMessage.setText(ERROR_CARAS);
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
            int valor = 0;
            for (int i = 0; i < numberOfDice; i++) {
                do {
                    valor = (int) (Math.random() * numberOfSides + 1);
                    dice.add(valor);
                    rollResult += valor;
                } while (valor < target);
            }
            sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "<" + target + FLECHA_DERECHA + dice + IGUAL
                    + rollResult + "*");
        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void savageWorldRoll(String command) {
        // "(s|savage)(4|6|8|10|12)"
        // roll {1d6, 1d8}kh1
        String[] numericParts = command.split("\\D+");
        numberOfSides = Integer.parseInt(numericParts[1]);
        rollResult = 0;

        ArrayList<Integer> wildDice = new ArrayList<>();
        int wild = 0;
        int wildResult = 0;
        do {
            wild = (int) (Math.random() * 6 + 1);
            wildDice.add(wild);
            wildResult += wild;
            System.out.println("wild" + wild);
        } while (wild == 6);

        ArrayList<Integer> dice = new ArrayList<>();
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
            sendMessage.setText("*Savage Worlds [6] [4] -2*\nSalvaje: [" + wildDice + RESULTADO_MENOS_DOS + (wildResult - 2)
                    + HABILIDAD + dice + RESULTADO_MENOS_DOS + (rollResult - 2) + TOTAL + (finalResult - 2) + " -> "
                    + (increases - 1) + AUMENTOS);
        } else {
            sendMessage.setText("*Savage Worlds [6] [" + numberOfSides + "]*\nSalvaje: [" + wildDice + "] = "
                    + wildResult + HABILIDAD + dice + "] = " + rollResult + TOTAL + finalResult + " -> "
                    + (increases - 1) + AUMENTOS);
        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void negativeSavageWorldRoll() {
        // "(s|savage)(-2)"
        // roll {1d6-2, 1d8-2}kh1
        numberOfSides = 4;
        rollResult = 0;

        ArrayList<Integer> wildDice = new ArrayList<>();
        int wild = 0;
        int wildResult = 0;
        do {
            wild = (int) (Math.random() * 6 + 1);
            wildDice.add(wild);
            wildResult += wild;
            System.out.println("wild" + wild);
        } while (wild == 6);

        ArrayList<Integer> dice = new ArrayList<>();
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
        sendMessage.setText("*Savage Worlds [6] [4] -2*\nSalvaje: [" + wildDice + RESULTADO_MENOS_DOS + (wildResult - 2)
                + HABILIDAD + dice + RESULTADO_MENOS_DOS + (rollResult - 2) + TOTAL + (finalResult - 2) + " -> "
                + (increases - 1) + AUMENTOS);

        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void inNomineSatanisRoll() {
        // 3d6
        rollResult = 0;
        numberOfDice = 3;
        numberOfSides = 6;
        String roll = "";
        for (int i = 0; i < numberOfDice; i++) {
            int valor = (int) (Math.random() * numberOfSides + 1);
            roll += valor;
        }
        if (roll.equalsIgnoreCase("111")) {
            sendMessage.setText("*[In Nomine Satanis]* = *👼" + roll + "👼*");
            sendVideo.setVideo("https://media.giphy.com/media/l2QE7PACf4cJccwA8/giphy.gif");
            try {
                execute(sendVideo);
            } catch (TelegramApiException e) {
                logger.log(Level.WARNING, e.getMessage());
            }
        } else if (roll.equalsIgnoreCase("666")) {
            sendMessage.setText("*[In Nomine Satanis]* = *😈" + roll + "😈*");
            sendVideo.setVideo("https://media.giphy.com/media/hB5vNhUepvcek/giphy.mp4");
            try {
                execute(sendVideo);
            } catch (TelegramApiException e) {
                logger.log(Level.WARNING, e.getMessage());
            }
        } else {
            sendMessage.setText("*[In Nomine Satanis]* = " + roll);
        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
        sendPhoto.setPhoto("https://i.imgur.com/PWsDzmW.jpg");
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            logger.log(Level.WARNING, e.getMessage());
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
            sendMessage.setText(ERROR_DEMASIADOS_DADOS);
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
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
                sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + FLECHA_DERECHA + dice
                        + IGUAL + (success - ones) + " éxito/s*");
            } else if (success < ones) {
                sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + FLECHA_DERECHA + dice
                        + RESULTADO_PIFIA);
                sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + FLECHA_DERECHA + dice
                        + RESULTADO_PIFIA);
            } else {
                sendMessage.setText(
                        "*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + FLECHA_DERECHA + dice + "] = *Fallo*");
            }
        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void vampireRollWithCustomDificulty(String command) {
        // v6d3
        String[] numericParts = command.split("\\D+");
        numberOfDice = Integer.parseInt(numericParts[1]);
        int difficulty = Integer.parseInt(numericParts[2]);
        if (difficulty < 2 || difficulty > 10) {
            difficulty = Integer.parseInt(numericParts[2]);
        }
        numberOfSides = 10;
        int success = 0;
        int ones = 0;
        if ((numberOfDice <= 0) || (numberOfDice > 50)) {
            sendMessage.setText(ERROR_DEMASIADOS_DADOS);
        } else if (difficulty < 2 || difficulty > 10) {
            sendMessage.setText("La dificultad ha de ser mínimo de 2 y máximo de 10");
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
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
                sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + FLECHA_DERECHA + dice
                        + IGUAL + (success - ones) + " éxito/s*");
            } else if (success < ones) {
                sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + FLECHA_DERECHA + dice
                        + RESULTADO_PIFIA);
                sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + FLECHA_DERECHA + dice
                        + RESULTADO_PIFIA);
            } else {
                sendMessage.setText(
                        "*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + FLECHA_DERECHA + dice + "] = *Fallo*");
            }

        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void fateRoll() {
        numberOfDice = 4;
        numberOfSides = 6;
        rollResult = 0;
        ArrayList<Integer> dice = new ArrayList<>();
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
        sendMessage.setParseMode(MARKDOWN);
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
        ArrayList<Integer> dice = new ArrayList<>();
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
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void hitosRoll() {
        // 3d10
        numberOfDice = 3;
        numberOfSides = 10;
        rollResult = 0;

        ArrayList<Integer> dice = new ArrayList<>();
        for (int i = 0; i < numberOfDice; i++) {
            int valor = (int) (Math.random() * numberOfSides + 1);
            dice.add(valor);
            rollResult += valor;
        }
        Collections.sort(dice);
        if ((dice.get(0) == 1) && (dice.get(1) == 1)) {
            if (dice.get(2) == 1) {
                sendMessage.setText(JUEGO_HITOS + dice.get(0) + "," + dice.get(1) + "," + dice.get(2)
                        + RESULTADO_PIFIA_TRIPLE);
                sendMessage.setText(JUEGO_HITOS + dice.get(0) + "," + dice.get(1) + "," + dice.get(2)
                        + RESULTADO_PIFIA_TRIPLE);
            } else {
                sendMessage.setText(JUEGO_HITOS + dice.get(0) + "," + dice.get(1) + "," + dice.get(2)
                        + RESULTADO_PIFIA_DOBLE);
                sendMessage.setText(JUEGO_HITOS + dice.get(0) + "," + dice.get(1) + "," + dice.get(2)
                        + RESULTADO_PIFIA_DOBLE);
            }
        } else if ((dice.get(0) == 10) && (dice.get(1) == 10)) {
            if (dice.get(2) == 10) {
                sendMessage.setText(
                        JUEGO_HITOS + dice.get(0) + "," + dice.get(1) + "," + dice.get(2) + "] -> * Éxito Triple*");
            } else {
                sendMessage.setText(
                        JUEGO_HITOS + dice.get(0) + "," + dice.get(1) + "," + dice.get(2) + "] -> * Éxito Doble*");
            }
        } else {
            sendMessage.setText(JUEGO_HITOS + dice.get(0) + "," + dice.get(1) + "," + dice.get(2) + IGUAL
                    + dice.get(1) + "*");
        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void hitosRollWithModifier(String command) {
        // 3d10+7 3d10-62
        String[] numericParts = command.split("\\D+");
        numberOfDice = 3;
        numberOfSides = 10;
        rollResult = 0;
        rollModifier = Integer.parseInt(numericParts[1]);
        ArrayList<Integer> dice = new ArrayList<>();
        for (int i = 0; i < numberOfDice; i++) {
            int valor = (int) (Math.random() * numberOfSides + 1);
            dice.add(valor);
            rollResult += valor;
        }
        Collections.sort(dice);
        if ((dice.get(0) == 1) && (dice.get(1) == 1)) {
            if (dice.get(2) == 1) {
                sendMessage.setText(JUEGO_HITOS + rollModifier + FLECHA_DERECHA + dice.get(0) + "," + dice.get(1) + ","
                        + dice.get(2) + RESULTADO_PIFIA_TRIPLE);
            } else {
                sendMessage.setText(JUEGO_HITOS + rollModifier + FLECHA_DERECHA + dice.get(0) + "," + dice.get(1) + ","
                        + dice.get(2) + RESULTADO_PIFIA_DOBLE);
            }
        } else if ((dice.get(1) == 10) && (dice.get(2) == 10)) {
            if (dice.get(0) == 10) {
                sendMessage.setText(JUEGO_HITOS + rollModifier + FLECHA_DERECHA + dice.get(0) + "," + dice.get(1) + ","
                        + dice.get(2) + "] -> * Éxito Triple*");
            } else {
                sendMessage.setText(JUEGO_HITOS + rollModifier + FLECHA_DERECHA + dice.get(0) + "," + dice.get(1) + ","
                        + dice.get(2) + "] -> * Éxito Doble*");
            }
        } else {
            sendMessage.setText(JUEGO_HITOS + rollModifier + FLECHA_DERECHA + dice.get(0) + "," + dice.get(1) + ","
                    + dice.get(2) + IGUAL + (dice.get(1) + rollModifier) + "*");
        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();

    }

    private void poweredByTheApocapilse() {
        // pbta p
        numberOfDice = 2;
        numberOfSides = 6;
        rollResult = 0;
        ArrayList<Integer> dice = new ArrayList<>();
        for (int i = 0; i < numberOfDice; i++) {
            int valor = (int) (Math.random() * numberOfSides + 1);
            dice.add(valor);
            rollResult += valor;
        }
        Collections.sort(dice);
        String result = "";
        if (rollResult < 7) {
            result = RESULTADO_FALLO;
        } else if (rollResult < 10) {
            result = RESULTADO_EXITO_PARCIAL;
        } else {
            result = RESULTADO_EXITO;
        }

        sendMessage.setText(JUEGO_PBTA + numberOfDice + "d" + numberOfSides + FLECHA_DERECHA + dice + IGUAL + rollResult
                + "->" + result + "*");

        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void poweredByTheApocapilseWithModifier(String command) {
        // pbta+1 p-1
        String[] numericParts = command.split("\\D+");
        numberOfDice = 2;
        numberOfSides = 6;
        rollModifier = Integer.parseInt(numericParts[1]);
        rollResult = 0;
        char modifier = '+';
        if (command.contains("-")) {
            modifier = '-';
        }
        ArrayList<Integer> dice = new ArrayList<>();
        for (int i = 0; i < numberOfDice; i++) {
            int valor = (int) (Math.random() * numberOfSides + 1);
            dice.add(valor);
            rollResult += valor;
        }

        Collections.sort(dice);
        String result = "";
        if (modifier == '+') {
            if (rollResult + rollModifier < 7) {
                result = RESULTADO_FALLO;
            } else if (rollResult + rollModifier < 10) {
                result = RESULTADO_EXITO_PARCIAL;
            } else {
                result = RESULTADO_EXITO;
            }
        } else {
            if (rollResult - rollModifier < 7) {
                result = RESULTADO_FALLO;
            } else if (rollResult - rollModifier < 10) {
                result = RESULTADO_EXITO_PARCIAL;
            } else {
                result = RESULTADO_EXITO;
            }
        }
        if (modifier == '+') {
            sendMessage.setText(
                    JUEGO_PBTA + numberOfDice + "d" + numberOfSides + "" + modifier + rollModifier + FLECHA_DERECHA + dice
                            + "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "->" + result + "*");
        } else if (modifier == '-') {
            sendMessage.setText(
                    JUEGO_PBTA + numberOfDice + "d" + numberOfSides + "" + modifier + rollModifier + FLECHA_DERECHA + dice
                            + "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "->" + result + "*");
        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }

    private void tiradaEriandos(String command) {
        String[] numericParts = command.split("\\D+");
        numberOfDice = 1;
        numberOfSides = 100;
        if (numericParts.length > 1) {
            rollModifier = Integer.parseInt(numericParts[1]);
        } else {
            rollModifier = 0;
        }
        rollResult = 0;
        int critico = 97;
        int pifia = 3;
        char modifier = '+';
        if (command.contains("-")) {
            modifier = '-';
        }
        boolean repitePifia = false;

        if ((numberOfDice <= 0) || (numberOfDice > 50)) {
            sendMessage.setText(ERROR_DEMASIADOS_DADOS);
        } else if (numberOfSides <= 1) {
            sendMessage.setText(ERROR_CARAS);
        } else {
            ArrayList<Integer> dice = new ArrayList<>();
            int valor = 0;
            valor = (int) (Math.random() * numberOfSides + 1);
            dice.add(valor);
            rollResult += valor;
            if (valor >= critico) {
                valor = (int) (Math.random() * numberOfSides + 1);
                dice.add(valor);
                rollResult += valor;
            } else if (valor <= pifia) {
                valor = (int) (Math.random() * numberOfSides + 1);
                dice.add(valor);
                rollResult -= valor;
                repitePifia = true;
            }
            if (rollModifier == 0) {
                if (dice.size() > 1) {
                    if (repitePifia) {
                        sendMessage.setText(JUEGO_ERIANDOS + numberOfDice + "d" + numberOfSides + FLECHA_DERECHA + dice.get(0) + " -" + dice.get(1) + IGUAL + (rollResult) + "*");
                    } else {
                        sendMessage.setText(JUEGO_ERIANDOS + numberOfDice + "d" + numberOfSides + FLECHA_DERECHA + dice.get(0) + " + " + dice.get(1) + IGUAL + (rollResult) + "*");
                    }
                } else {
                    sendMessage.setText(JUEGO_ERIANDOS + numberOfDice + "d" + numberOfSides + FLECHA_DERECHA + dice + IGUAL + (rollResult) + "*");
                }

            } else if (modifier == '+') {
                if (dice.size() > 1) {
                    if (repitePifia) {
                        sendMessage.setText(JUEGO_ERIANDOS + numberOfDice + "d" + numberOfSides + " + " + rollModifier + FLECHA_DERECHA + dice.get(0) + " -" + dice.get(1) + " + " + rollModifier + IGUAL + (rollResult + rollModifier) + "*");
                    } else {
                        sendMessage.setText(JUEGO_ERIANDOS + numberOfDice + "d" + numberOfSides + " + " + rollModifier + FLECHA_DERECHA + dice.get(0) + " + " + dice.get(1) + " + " + rollModifier + IGUAL + (rollResult + rollModifier) + "*");
                    }
                } else {
                    sendMessage.setText(JUEGO_ERIANDOS + numberOfDice + "d" + numberOfSides + " + " + rollModifier + "]*-> " + dice + " + " + rollModifier + " = *" + (rollResult + rollModifier) + "*");
                }
            } else {
                if (dice.size() > 1) {
                    if (repitePifia) {
                        sendMessage.setText(JUEGO_ERIANDOS + numberOfDice + "d" + numberOfSides + " - " + rollModifier + FLECHA_DERECHA + dice.get(0) + " -" + dice.get(1) + " - " + rollModifier + IGUAL + (rollResult - rollModifier) + "*");
                    } else {
                        sendMessage.setText(JUEGO_ERIANDOS + numberOfDice + "d" + numberOfSides + " - " + rollModifier + FLECHA_DERECHA + dice.get(0) + " + " + dice.get(1) + " - " + rollModifier + IGUAL + (rollResult - rollModifier) + "*");
                    }
                } else {
                    sendMessage.setText(JUEGO_ERIANDOS + numberOfDice + "d" + numberOfSides + " - " + rollModifier + "]*-> " + dice + " - " + rollModifier + " = *" + (rollResult - rollModifier) + "*");
                }
            }
        }
        sendMessage.setParseMode(MARKDOWN);
        answerUser();
    }
}
