package gui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;

import javax.swing.JTextArea;

class TextComponentHandler extends java.util.logging.Handler {
	private JTextArea textArea = new JTextArea(10, 10);


	@Override
	public void publish(final LogRecord record) {
//		SwingUtilities.invokeLater(new Runnable() {
//
//			@Override
//			public void run() {
				StringWriter text = new StringWriter();
                PrintWriter out = new PrintWriter(text);
                out.println(textArea.getText());
//                out.printf("[%s] [Thread-%d]: %s.%s -> %s", record.getLevel(),
//                        record.getThreadID(), record.getSourceClassName(),
//                        record.getSourceMethodName(), record.getMessage());
                out.printf("%s", record.getMessage());
                textArea.setText(text.toString());
//			}
//
//		});
	}

	@Override
	public void flush() {/**/
	}

	@Override
	public void close() throws SecurityException {/**/
	}
	
	public JTextArea getTextArea() {
        return this.textArea;
    }
}