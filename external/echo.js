const args = Interop.import('arguments');
const graph = Interop.import('graph');
const log = Interop.import('log');

function main() { 
   console.log('JS is alive and is processing ', args, ' in ', process.cwd());
   return args;
} 

main();
