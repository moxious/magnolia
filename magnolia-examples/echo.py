# https://stackoverflow.com/questions/51802367/getting-outer-environment-arguments-from-java-using-graal-python
import polyglot

def main():
  log = polyglot.import_value('log')
  log.info("echo-python running")
  return polyglot.import_value('arguments')

main()
