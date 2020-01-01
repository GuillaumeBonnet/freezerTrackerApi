const clipboardy = require('clipboardy');

let tableNames = ['users', 'aliment', 'freezer', 'verification_token'];
function toTitleCase(inputString) {
    return inputString[0].toUpperCase() + inputString.substring(1);
}


let resultingString = '';

    for(tableName of tableNames) {
        let titleCasedName = toTitleCase(tableName);
        let triggerTemplate = `
            -- # ---------------------------------------------------------------------------- #
            -- #          - Trigger: beforeUpdate${titleCasedName}
            -- # ---------------------------------------------------------------------------- #

                DROP TRIGGER "beforeUpdate${titleCasedName}" ON public.${tableName};

                CREATE TRIGGER "beforeUpdate${titleCasedName}"
                    BEFORE UPDATE 
                    ON public.${tableName}
                    FOR EACH ROW
                    EXECUTE PROCEDURE public.update_ts_func();

            -- # ---------------------------------------------------------------------------- #
            -- #           - Trigger: beforeInsert${titleCasedName}
            -- # ---------------------------------------------------------------------------- #

                DROP TRIGGER "beforeInsert${titleCasedName}" ON public.${tableName};

                CREATE TRIGGER "beforeInsert${titleCasedName}"
                    BEFORE INSERT
                    ON public.${tableName}
                    FOR EACH ROW
                    EXECUTE PROCEDURE public.insert_ts_func();`
        ;
        resultingString += triggerTemplate;
    }
    
    // Copy
    clipboardy.writeSync(resultingString);
    console.log('script SQL-generateTriggers filled the clipboard with the SQL script');