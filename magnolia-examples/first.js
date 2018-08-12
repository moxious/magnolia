const args = Interop.import('arguments');
const graph = Interop.import('graph');
const log = Interop.import('log');

function main() { 
   console.log('JS is alive and is processing ', args);
   return { 
     id: args.id, 
     text: args.text, 
     arr: args.arr, 
     gimme: args.gimme ? args.gimme() : null,
     graph: graph,
     log: log
   }; 
} 

main();
