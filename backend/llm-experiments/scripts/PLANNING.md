# About these scripts

The goal is to have a collection of small functions that help me:

- Prepare prompts and context
- Make queries to LLM services, selecting models, options and passing prepared prompts
- Parse the JSON responses of LLM services
- Process the LLM output messages

Each step is likely to evolve and become more complex or split into variations of the same process. For example, preparing prompts may require more variables or even functions, and parsing could require a different process between providers and models.

WAIT. YAGNI risk.

What do I actually need, like, right now?

- To be able to test different prompts (text files are enough)
- Call LLMs, select model and pass prompt (convert do-prompt to function)
- Parse JSON output (cheshire is enough)
- Extract message (get-in will do)
- Remove thinking
- Store results for analysis (each with prompt, model, response and timestamp),h
