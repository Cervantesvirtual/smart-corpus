package com.cervantesvirtual.index;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.cervantesvirtual.analyzer.SynonymIndexAnalyzer;
import com.cervantesvirtual.analyzer.TextAnalyzer;

/** Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing.
 * Run it with no command-line arguments for usage information.
 */
public class IndexFiles {
  
  private IndexFiles() {}
  
  public static IndexWriterConfig getLuceneConfig() {
     Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
     analyzerMap.put("contents-simple", new TextAnalyzer());
	   
     PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new SynonymIndexAnalyzer(), analyzerMap);
		
     IndexWriterConfig luceneConfig = new IndexWriterConfig(wrapper);
     return luceneConfig;
  }

  /** Index all text files under a directory. */
  public static void main(String[] args) {
    String indexPath = "index";
    String docsPath = "src/main/resources/txtfiles";
    boolean create = true;

    final Path docDir = Paths.get(docsPath);
    if (!Files.isReadable(docDir)) {
      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    
    Date start = new Date();
    try {
      System.out.println("Indexing to directory '" + indexPath + "'...");

      Directory dir = FSDirectory.open(Paths.get(indexPath));
      //Analyzer analyzer = new SynonymIndexAnalyzer();
      //IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
      
      IndexWriterConfig iwc = IndexFiles.getLuceneConfig();

      if (create) {
        // Create a new index in the directory, removing any
        // previously indexed documents:
        iwc.setOpenMode(OpenMode.CREATE);
      } else {
        // Add new documents to an existing index:
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
      }

      // Optional: for better indexing performance, if you
      // are indexing many documents, increase the RAM
      // buffer.  But if you do this, increase the max heap
      // size to the JVM (eg add -Xmxm or -Xmxg):
      //
      // iwc.setRAMBufferSizeMB(.);

      IndexWriter writer = new IndexWriter(dir, iwc);
      indexDocs(writer, docDir);

      // NOTE: if you want to maximize search performance,
      // you can optionally call forceMerge here.  This can be
      // a terribly costly operation, so generally it's only
      // worth it when your index is relatively static (ie
      // you're done adding documents to it):
      //
      // writer.forceMerge();

      writer.close();

      Date end = new Date();
      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    } catch (IOException e) {
      System.out.println(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
    }
  }

  /**
   * Indexes the given file using the given writer, or if a directory is given,
   * recurses over files and directories found under the given directory.
   * 
   * NOTE: This method indexes one document per input file.  This is slow.  For good
   * throughput, put multiple documents into your input file(s).  An example of this is
   * in the benchmark module, which can create "line doc" files, one document per line,
   * using the
   * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
   * >WriteLineDocTask</a>.
   *  
   * @param writer Writer to the index where the given file/dir info will be stored
   * @param path The file to index, or the directory to recurse into to find files to index
   * @throws IOException If there is a low-level I/O error
   */
  static void indexDocs(final IndexWriter writer, Path path) throws IOException {
    if (Files.isDirectory(path)) {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          try {
            indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
          } catch (IOException ignore) {
            // don't index files that can't be read.
          }
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
      indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
    }
  }

  /** Indexes a single document */
  static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
    try (InputStream stream = Files.newInputStream(file)) {
      // make a new, empty document
      Document doc = new Document();
      
      // Add the path of the file as a field named "path".  Use a
      // field that is indexed (i.e. searchable), but don't tokenize 
      // the field into separate words and don't index term frequency
      // or positional information:
      Field pathField = new StringField("path", file.toString(), Field.Store.YES);
      doc.add(pathField);
      
      String title = "";
      String author = "";
      String slug = "";
      String wikidata = "";
      String workdata = "";
      if("src/main/resources/txtfiles/ff498544-82b1-11df-acc7-002185ce6064.txt".equals(file.toString())){
    	  title = "Doña perfecta";
    	  author = "Pérez Galdós, Benito";
    	  slug = "dona-perfecta-novela-original--0";
    	  wikidata = "Q476558";
    	  workdata = "19037";
      }
      if("src/main/resources/txtfiles/ejemplo.txt".equals(file.toString())){
    	  title = "Ejemplo";
    	  author = "Gustavo Candela";
    	  slug = "dona-perfecta-novela-original--0";
    	  wikidata = "Q476558";
    	  workdata = "19037";
      }
      System.out.println("Indexing:" + title);
            
      Field titleField = new StringField("title", title, Field.Store.YES);
      doc.add(titleField);
      
      Field authorField = new StringField("author", author, Field.Store.YES);
      doc.add(authorField);
      
      Field slugField = new StringField("slug", slug, Field.Store.YES);
      doc.add(slugField);
      
      Field workdataField = new StringField("workdata", workdata, Field.Store.YES);
      doc.add(workdataField);
      
      Field wikidataField = new StringField("wikidata", wikidata, Field.Store.YES);
      doc.add(wikidataField);
      
      // Add the last modified date of the file a field named "modified".
      // Use a LongPoint that is indexed (i.e. efficiently filterable with
      // PointRangeQuery).  This indexes to milli-second resolution, which
      // is often too fine.  You could instead create a number based on
      // year/month/day/hour/minutes/seconds, down the resolution you require.
      // For example the long value  would mean
      // February , , - PM.
      doc.add(new LongPoint("modified", lastModified));
      
      // Add the contents of the file to a field named "contents".  Specify a Reader,
      // so that the text of the file is tokenized and indexed, but not stored.
      // Note that FileReader expects the file to be in UTF- encoding.
      // If that's not the case searching for special characters will fail.
      doc.add(new TextField("contents-simple", new String(Files.readAllBytes(file)), Store.YES));
      doc.add(new TextField("contents", new String(Files.readAllBytes(file)), Store.YES));
      
      if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
        // New index, so we just add the document (no old document can be there):
        System.out.println("adding " + file);
        writer.addDocument(doc);
      } else {
        // Existing index (an old copy of this document may have been indexed) so 
        // we use updateDocument instead to replace the old one matching the exact 
        // path, if present:
        System.out.println("updating " + file);
        writer.updateDocument(new Term("path", file.toString()), doc);
      }
    }
  }
}