import re
import urllib
import pandas as pd
import numpy as np
import itertools

text_match = '([\w.&\ -]+)'
number_match = '(?:<font color=red>)?([-+]?[0-9]*\.?[0-9]*)(?:</font>)?'

# Download the results from the ECS TAC server

def game_results(game, exclude_games=[]):
    results = {}
    if game not in exclude_games:
        f = urllib.urlopen("http://tac.ecs.soton.ac.uk:8080/history/"+str(game)+"/")
        s = str(f.read())
        f.close()
        if s.count('HTTP ERROR')==0:
            print("Reading game " + str(game))
            r = re.findall(r'(<tr><td>'+text_match+'</td><td>'+number_match+'</td><td>'+number_match+'</td><td>'+number_match+'</td>)', s)
            for s, name, util, cost, score in r:
                results[name] = {"utility": float(util), "cost": float(cost), "score": float(score)}
        else:
            print("Skipping game " + str(game))
    else:
        print("Skipping game " + str(game))
    return results


def all_game_results(from_game=613, to_game=656):
    return [game_results(game_num) for game_num in range(from_game, to_game)]
    
def flatten_results(list_scores):
    return list(itertools.chain(*[list(gs.items()) for gs in list_scores]))

def filter_agent(flat_scores, name):
    return list(filter(lambda x: x[0] == name, flat_scores))

def discard_agent(flat_scores):
    return list(map(lambda x: x[1], flat_scores))

def just_scores(flat_scores):
    return list(map(lambda x: x['score'], discard_agent(flat_scores)))