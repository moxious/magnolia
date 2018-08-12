const args = Interop.import('arguments');
const graph = Interop.import('graph');
const log = Interop.import('log');

function main() { 
   if (log) {
      console.log(log);
      log.info('JavaScript functions can call Java loggers!');
   }

   console.log('JS is alive and is processing ', args);
   return true; 
} 

main();
