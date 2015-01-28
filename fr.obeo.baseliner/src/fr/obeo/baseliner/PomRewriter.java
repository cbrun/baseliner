package fr.obeo.baseliner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.XMLEvent;

import org.osgi.framework.Version;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

public class PomRewriter {

	private static final QName mavenProject = new QName("http://maven.apache.org/POM/4.0.0", "project");
	private static final QName mavenVersion = new QName("http://maven.apache.org/POM/4.0.0", "version");

	private Optional<Version> pluginVersion = Optional.absent();

	public void setPluginVersion(Version pluginVersion) {
		this.pluginVersion = Optional.fromNullable(pluginVersion);
	}

	public void rewrite(File inPom) throws XMLStreamException, IOException {
		/*
		 * in its current implementation we only rewrite the version, if none
		 * specified, nothing to do.
		 */
		if (pluginVersion.isPresent()) {
			XMLInputFactory inFactory = XMLInputFactory.newInstance();

			FileInputStream fIs = null;
			BufferedInputStream bufferedStream = null;
			try {
				fIs = new FileInputStream(inPom);
				bufferedStream = new BufferedInputStream(fIs);
				XMLEventReader eventReader = inFactory.createXMLEventReader(bufferedStream);

				XMLOutputFactory factory = XMLOutputFactory.newInstance();

				/*
				 * XMLStreamWriter streamWriter =
				 * factory.createXMLStreamWriter(new BufferedOutputStream(new
				 * FileOutputStream(new File( inPom.getParent(),
				 * "pom-rewrite.xml"))))
				 */

				StringWriter outBuffer = new StringWriter();
				XMLEventWriter writer = factory.createXMLEventWriter(outBuffer);
				XMLEventFactory eventFactory = XMLEventFactory.newInstance();

				String foundEncoding = null;
				Stack<QName> currentParentName = new Stack<QName>();
				boolean shouldReplaceText = false;
				while (eventReader.hasNext()) {
					XMLEvent event = eventReader.nextEvent();
					if (event.getEventType() == XMLEvent.START_DOCUMENT) {
						if (foundEncoding == null) {
							foundEncoding = ((StartDocument) event).getCharacterEncodingScheme();
						}
					}
					if (event.getEventType() == XMLEvent.COMMENT) {
						XMLEvent carriage = eventFactory.createSpace("\n");
						writer.add(carriage);
						writer.add(event);
						writer.add(carriage);
					} else if (event.getEventType() == XMLEvent.START_ELEMENT) {

						QName tagName = event.asStartElement().getName();

						if (mavenVersion.equals(tagName) && currentParentName.size() > 0
								&& mavenProject.equals(currentParentName.peek())) {
							shouldReplaceText = true;
							writer.add(event);
						} else {
							writer.add(event);
						}
						currentParentName.push(tagName);

					} else if (event.getEventType() == XMLEvent.END_ELEMENT) {
						currentParentName.pop();
						shouldReplaceText = false;
						writer.add(event);
					} else if (event.getEventType() == XMLEvent.CHARACTERS) {
						if (shouldReplaceText && pluginVersion.isPresent()) {
							String version = this.pluginVersion.get().toString().replace(".qualifier", "-SNAPSHOT");
							writer.add(eventFactory.createCharacters(version));
						} else {
							writer.add(event);
						}
					}

					else {
						writer.add(event);
					}

				}
				eventReader.close();
				writer.close();
				if (foundEncoding == null || !Charset.isSupported(foundEncoding)) {
					foundEncoding = Charsets.UTF_8.name();
				}
				Files.write(outBuffer.toString(), inPom, Charset.forName(foundEncoding));
			} finally {
				Closeables.closeQuietly(fIs);
				Closeables.closeQuietly(bufferedStream);
			}
		}
	}
}