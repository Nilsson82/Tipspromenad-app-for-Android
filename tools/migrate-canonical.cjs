// One-time, explicit migration. Never regenerate an existing published revision.
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '..');
const source = path.join(root, 'related-projects/TipspromenadQuizWebPage/Data');
const target = path.join(root, 'related-projects/Tipspromenad/database');
fs.mkdirSync(target, {recursive:true});
if (fs.existsSync(path.join(target, 'revision-1.json'))) throw Error('Revision 1 already exists; append a new revision instead.');
const questions = [], mappings = [];
const categories = ['geography','history','culture','sport','mathematics','science','space','nature'];
const worldCategories = ['geography','geography','geography','geography','geography','geography','geography','geography','geography','history','culture','sport'];
for (let group=0; group<2; group++) for(let i=0;i<12;i++) {
  const questionId=group*12+i+1;
  const q={questionId,category:group===0?worldCategories[i]:([4].includes(i)?'sport':[5].includes(i)?'history':[8,9,11].includes(i)?'culture':'geography'),subcategory:group===0?'world':'venezuela',difficulty:2,tags:[],reviewStatus:'legacy-unreviewed',translations:{}};
  // Ambiguous/outdated source questions remain preserved but are not offered for new quizzes.
  q.deprecated=[3,5,8,18,22].includes(questionId);
  for(const language of ['en','sv','es']) {
    const listIndex=language==='es'?1-group:group;
    const old=JSON.parse(fs.readFileSync(path.join(source,`data_${language}.json`)))[listIndex].QuestionList[i];
    const options=[...old.answers]; if(options.length===3) options.push('Mont Blanc');
    q.translations[language]={question:old.question,options,correctIndex:options.indexOf(old.correctAnswer)};
    mappings.push({questionId,source:`data_${language}.json`,listIndex,questionIndex:i,addedDistractor:old.answers.length===3?'Mont Blanc':null});
  }
  questions.push(q);
}
const starter=JSON.parse(fs.readFileSync(path.join(source,'multilingual.json')));
const languages=['en','sv','es','da','no','fi'];
const extra=[['Six','Sex','Seis','Seks','Seks','Kuusi'],['NaCl','NaCl','NaCl','NaCl','NaCl','NaCl'],['Jupiter','Jupiter','Júpiter','Jupiter','Jupiter','Jupiter'],['Venus','Venus','Venus','Venus','Venus','Venus'],['Nitrogen','Kväve','Nitrógeno','Kvælstof','Nitrogen','Typpeä'],['Ten','Tio','Diez','Ti','Ti','Kymmenen']];
starter.questions.forEach((old,i)=>{
 const q={questionId:25+i,category:old.category,subcategory:old.tags[0],difficulty:1,tags:old.tags,reviewStatus:'translated-unreviewed',deprecated:false,translations:{}};
 languages.forEach((lang,j)=>{const t=old.translations[lang];q.translations[lang]={question:t.question,options:[...t.answers.map(a=>a.text),extra[i][j]],correctIndex:t.answers.findIndex(a=>a.id===old.correctAnswerId)};});
 questions.push(q);mappings.push({questionId:q.questionId,source:'multilingual.json',legacyId:old.id,addedDistractors:extra[i]});
});
fs.writeFileSync(path.join(target,'revision-1.json'),JSON.stringify({schemaVersion:2,revision:1,categories,questions},null,2)+'\n');
fs.writeFileSync(path.join(target,'id-registry.json'),JSON.stringify({nextId:31,neverReuse:true,mappings},null,2)+'\n');
