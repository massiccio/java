/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package utils;


/**
 * IOUtils
 * 
 * @(#)file IOUtils.java
 * @(#)author <a href="mailto:michelemazzucco@gmail.com">Michele Mazzucco</a>
 * @(#)version 0.1
 * @(#)created Dec 8, 2011
 * @(#)lastedit Dec 8, 2011
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;


/**
 * IO utilities.
 */
public class IOUtils {
	
	/**
     * Returns the last line of a text file.
     * 
     * @param file The file.
     * @return The last line.
     * @see {@link http
     *      ://stackoverflow.com/questions/686231/java-quickly-read-the
     *      -last-line-of-a-text-file}
     */
    public static String tail(File file) {
    	RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = file.length() - 1;
            StringBuilder sb = new StringBuilder();

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer == fileLength) {
                        continue;
                    }
                    break;
                } else if (readByte == 0xD) {
                    if (filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }

                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
        	try {
				fileHandler.close();
			} catch (IOException e) {
				// ignore
			}
        }
    }


    /**
     * Counts the number of lines in a text file, excluding the lines starting
     * with the character '#'.
     * 
     * @param file The file.
     * @return The number of lines in the file.
     * @throws IOException If an error occurs while opening the file.
     */
    public static int count(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;

            boolean enabled = true;
            while ((readChars = is.read(c)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '#') {
                        enabled = false;
                    }
                    if (c[i] == '\n') {
                        if (enabled) {
                            ++count;
                        } else {
                            enabled = true;
                        }
                    }
                }
            }
            return count;
        } finally {
            is.close();
        }
    }


}
