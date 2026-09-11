// Uses Ajv already installed by the existing webpack project's npm ci.
const fs = require('node:fs');
const path = require('node:path');
const Ajv = require('../related-projects/Tipspromenad/node_modules/ajv/dist/2020');
const addFormats = require('../related-projects/Tipspromenad/node_modules/ajv-formats');
const root = path.resolve(__dirname, '..');
const read = file => JSON.parse(fs.readFileSync(path.join(root,file),'utf8'));
const ajv = new Ajv({allErrors:true,strict:false});
addFormats(ajv);
const bankSchema = read('docs/contracts/question-bank.schema.json');
const snapshotSchema = read('docs/contracts/quiz-snapshot.schema.json');
ajv.addSchema(bankSchema);
ajv.addSchema(snapshotSchema);
for (const [file, schema] of [
  ['docs/contracts/question-bank.example.json',bankSchema.$id],
  ['docs/contracts/quiz-snapshot.example.json',snapshotSchema.$id],
  ['related-projects/TipspromenadQuizWebPage/Data/multilingual.json',bankSchema.$id]
]) {
  if (!ajv.validate(schema,read(file))) throw new Error(`${file}: ${JSON.stringify(ajv.errors)}`);
  console.log(`${file}: valid`);
}
