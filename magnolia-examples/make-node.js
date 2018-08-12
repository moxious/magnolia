const args = Interop.import('arguments');
const graph = Interop.import('graph');
const log = Interop.import('log');
const Label = Java.type('org.neo4j.graphdb.Label');

function main() { 
   if (log) {
      console.log(log);
      log.info('Attempting to create a node in JS');
   }

   const tx = graph.beginTx();
   
   try {
     const label = Label.label('Magnolia');
     const node = graph.createNode(label);
     node.setProperty('created', ''+new Date());
     log.info('Successs!');
     tx.success();

     // Return a stream of the one node we created.
     const Arrays = Java.type('java.util.Arrays');
     return Arrays.asList(node).stream();
   } catch (e) {
     log.error('Failed to run transaction', e);
     tx.failure();
     tx.close();
     throw e;
   }
} 

main();
